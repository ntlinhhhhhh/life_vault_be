package com.moon.vault.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moon.vault.common.AppConstants;
import com.moon.vault.common.PageResp;
import com.moon.vault.dto.request.CreateVaultItemRequest;
import com.moon.vault.dto.request.RenewItemRequest;
import com.moon.vault.dto.request.SearchVaultItemRequest;
import com.moon.vault.dto.request.UpdateVaultItemRequest;
import com.moon.vault.dto.response.VaultItemResponse;
import com.moon.vault.entity.Tag;
import com.moon.vault.entity.VaultItem;
import com.moon.vault.entity.VaultItemTag;
import com.moon.vault.entity.VaultItemTagId;
import com.moon.vault.exception.VaultException;
import com.moon.vault.repository.FolderRepository;
import com.moon.vault.repository.TagRepository;
import com.moon.vault.repository.VaultItemRepository;
import com.moon.vault.repository.VaultItemTagRepository;
import com.moon.vault.security.JwtService;
import com.moon.vault.service.AuditLogService;
import com.moon.vault.service.EncryptionService;
import com.moon.vault.service.ReminderService;
import com.moon.vault.service.VaultItemService;
import com.moon.vault.util.CodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VaultItemServiceImpl implements VaultItemService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final VaultItemRepository vaultItemRepository;
    private final VaultItemTagRepository vaultItemTagRepository;
    private final TagRepository tagRepository;
    private final FolderRepository folderRepository;
    private final ObjectMapper objectMapper;
    private final EncryptionService encryptionService;
    private final AuditLogService auditLogService;
    private final JwtService jwtService;
    private final @Lazy ReminderService reminderService;

    @Override
    @Transactional(readOnly = true)
    public PageResp<VaultItemResponse> search(String userCode, SearchVaultItemRequest request) {
        int page = Math.max(request.page(), 0);
        int size = Math.min(Math.max(request.size() <= 0 ? 20 : request.size(), 1), 100);
        Set<String> tagItemCodes = itemCodesByTag(request.tagCode());
        List<VaultItemResponse> filtered = vaultItemRepository.findByUserCodeOrderByCreateDateDesc(userCode).stream()
                .filter(item -> matches(item, request, tagItemCodes))
                .map(item -> toResponse(item, false))
                .toList();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        int totalPages = (int) Math.ceil(filtered.size() / (double) size);
        return new PageResp<>(filtered.subList(from, to), page, size, filtered.size(), totalPages);
    }

    @Override
    @Transactional
    public VaultItemResponse create(String userCode, CreateVaultItemRequest request, HttpServletRequest httpRequest) {
        validateFolder(userCode, request.folderCode());
        VaultItem item = new VaultItem();
        item.setVaultItemCode(CodeGenerator.generate(AppConstants.ITEM_CODE_PREFIX));
        item.setUserCode(userCode);
        apply(item, request.title(), request.type(), request.category(), request.folderCode(), request.securityLevel(),
                request.description(), request.metadata(), request.sensitiveContent(), request.expirationDate(), userCode);
        item.setCreateUser(userCode);
        item = vaultItemRepository.save(item);
        assignTags(userCode, item.getVaultItemCode(), request.tagCodes());
        reminderService.ensureExpirationReminders(userCode, item.getVaultItemCode(), item.getExpirationDate());
        auditLogService.record(userCode, "CREATE_ITEM", "VAULT_ITEM", item.getVaultItemCode(), httpRequest);
        return toResponse(item, false);
    }

    @Override
    @Transactional
    public VaultItemResponse detail(String userCode, String itemCode, HttpServletRequest httpRequest) {
        VaultItem item = getItem(userCode, itemCode);
        boolean includeSensitive = !requiresExtraProtection(item);
        if (requiresExtraProtection(item)) {
            auditLogService.record(userCode, "VIEW_SECRET_ITEM", "VAULT_ITEM", itemCode, httpRequest);
        }
        return toResponse(item, includeSensitive);
    }

    @Override
    @Transactional
    public VaultItemResponse update(String userCode, String itemCode, UpdateVaultItemRequest request, HttpServletRequest httpRequest) {
        validateFolder(userCode, request.folderCode());
        VaultItem item = getItem(userCode, itemCode);
        apply(item, request.title(), request.type(), request.category(), request.folderCode(), request.securityLevel(),
                request.description(), request.metadata(), request.sensitiveContent(), request.expirationDate(), userCode);
        item.setUpdateUser(userCode);
        item = vaultItemRepository.save(item);
        assignTags(userCode, itemCode, request.tagCodes());
        reminderService.ensureExpirationReminders(userCode, itemCode, item.getExpirationDate());
        auditLogService.record(userCode, "UPDATE_ITEM", "VAULT_ITEM", itemCode, httpRequest);
        return toResponse(item, false);
    }

    @Override
    @Transactional
    public void delete(String userCode, String itemCode, HttpServletRequest httpRequest) {
        VaultItem item = getItem(userCode, itemCode);
        item.setStatus(AppConstants.STATUS_DELETED);
        item.setWorkflowState("deleted");
        item.setDeletedAt(LocalDateTime.now());
        item.setUpdateUser(userCode);
        vaultItemRepository.save(item);
        auditLogService.record(userCode, "DELETE_ITEM", "VAULT_ITEM", itemCode, httpRequest);
    }

    @Override
    @Transactional
    public VaultItemResponse restore(String userCode, String itemCode, HttpServletRequest httpRequest) {
        VaultItem item = vaultItemRepository.findByVaultItemCodeAndUserCode(itemCode, userCode)
                .orElseThrow(() -> VaultException.notFound("Vault item not found"));
        item.setStatus(AppConstants.STATUS_ACTIVE);
        item.setWorkflowState("active");
        item.setDeletedAt(null);
        item.setUpdateUser(userCode);
        auditLogService.record(userCode, "RESTORE_ITEM", "VAULT_ITEM", itemCode, httpRequest);
        return toResponse(vaultItemRepository.save(item), false);
    }

    @Override
    @Transactional
    public void purge(String userCode, String itemCode, String reauthToken, HttpServletRequest httpRequest) {
        jwtService.validateReauthToken(reauthToken, userCode);
        VaultItem item = vaultItemRepository.findByVaultItemCodeAndUserCode(itemCode, userCode)
                .orElseThrow(() -> VaultException.notFound("Vault item not found"));
        vaultItemTagRepository.deleteByIdVaultItemCode(itemCode);
        vaultItemRepository.delete(item);
        auditLogService.record(userCode, "PURGE_ITEM", "VAULT_ITEM", itemCode, httpRequest);
    }

    @Override
    @Transactional
    public VaultItemResponse toggleFavorite(String userCode, String itemCode, HttpServletRequest httpRequest) {
        VaultItem item = getItem(userCode, itemCode);
        item.setFavorite(!Boolean.TRUE.equals(item.getFavorite()));
        item.setUpdateUser(userCode);
        auditLogService.record(userCode, "FAVORITE_ITEM", "VAULT_ITEM", itemCode, httpRequest);
        return toResponse(vaultItemRepository.save(item), false);
    }

    @Override
    @Transactional
    public VaultItemResponse archive(String userCode, String itemCode, HttpServletRequest httpRequest) {
        VaultItem item = getItem(userCode, itemCode);
        item.setStatus(AppConstants.STATUS_ARCHIVED);
        item.setUpdateUser(userCode);
        auditLogService.record(userCode, "ARCHIVE_ITEM", "VAULT_ITEM", itemCode, httpRequest);
        return toResponse(vaultItemRepository.save(item), false);
    }

    @Override
    @Transactional
    public VaultItemResponse renew(String userCode, String itemCode, RenewItemRequest request, HttpServletRequest httpRequest) {
        VaultItem item = getItem(userCode, itemCode);
        item.setExpirationDate(request.newExpirationDate());
        item.setUpdateUser(userCode);
        item = vaultItemRepository.save(item);
        reminderService.ensureExpirationReminders(userCode, itemCode, item.getExpirationDate());
        auditLogService.record(userCode, "RENEW_ITEM", "VAULT_ITEM", itemCode, httpRequest);
        return toResponse(item, false);
    }

    private VaultItem getItem(String userCode, String itemCode) {
        return vaultItemRepository.findByVaultItemCodeAndUserCode(itemCode, userCode)
                .filter(item -> !"deleted".equalsIgnoreCase(item.getWorkflowState()))
                .orElseThrow(() -> VaultException.notFound("Vault item not found"));
    }

    private void apply(
            VaultItem item,
            String title,
            String type,
            String category,
            String folderCode,
            String securityLevel,
            String description,
            Map<String, Object> metadata,
            Map<String, Object> sensitiveContent,
            LocalDate expirationDate,
            String userCode
    ) {
        item.setTitle(title);
        item.setType(type);
        item.setCategory(category);
        item.setFolderCode(blankToNull(folderCode));
        item.setSecurityLevel(securityLevel == null || securityLevel.isBlank() ? AppConstants.SECURITY_NORMAL : securityLevel);
        item.setDescription(description);
        item.setMetadataJson(writeJson(metadata));
        if (sensitiveContent != null && !sensitiveContent.isEmpty()) {
            var encrypted = encryptionService.encryptString(writeJson(sensitiveContent));
            item.setEncryptedContent(encrypted.ciphertext());
            item.setEncryptionIv(encrypted.iv());
        }
        item.setExpirationDate(expirationDate);
        item.setUpdateUser(userCode);
    }

    private void assignTags(String userCode, String itemCode, List<String> tagCodes) {
        vaultItemTagRepository.deleteByIdVaultItemCode(itemCode);
        if (tagCodes == null || tagCodes.isEmpty()) {
            return;
        }
        List<Tag> tags = tagRepository.findByTagCodeInAndUserCode(tagCodes, userCode);
        if (tags.size() != new HashSet<>(tagCodes).size()) {
            throw VaultException.badRequest("One or more tags are invalid");
        }
        tags.forEach(tag -> {
            VaultItemTagId id = new VaultItemTagId();
            id.setVaultItemCode(itemCode);
            id.setTagCode(tag.getTagCode());
            VaultItemTag itemTag = new VaultItemTag();
            itemTag.setId(id);
            itemTag.setCreateUser(userCode);
            vaultItemTagRepository.save(itemTag);
        });
    }

    private boolean matches(VaultItem item, SearchVaultItemRequest request, Set<String> tagItemCodes) {
        if (request.status() == null && AppConstants.STATUS_DELETED.equalsIgnoreCase(item.getStatus())) {
            return false;
        }
        return containsIgnoreCase(item.getTitle(), request.q())
                && equalsIfPresent(item.getType(), request.type())
                && equalsIfPresent(item.getCategory(), request.category())
                && equalsIfPresent(item.getFolderCode(), request.folderCode())
                && equalsIfPresent(item.getSecurityLevel(), request.securityLevel())
                && equalsIfPresent(item.getStatus(), request.status())
                && (request.favorite() == null || request.favorite().equals(item.getFavorite()))
                && (request.nearExpired() == null || !request.nearExpired() || isNearExpired(item))
                && (request.tagCode() == null || tagItemCodes.contains(item.getVaultItemCode()));
    }

    private Set<String> itemCodesByTag(String tagCode) {
        if (tagCode == null || tagCode.isBlank()) {
            return Collections.emptySet();
        }
        return vaultItemTagRepository.findByIdTagCode(tagCode).stream()
                .map(itemTag -> itemTag.getId().getVaultItemCode())
                .collect(java.util.stream.Collectors.toSet());
    }

    private boolean isNearExpired(VaultItem item) {
        return item.getExpirationDate() != null
                && !item.getExpirationDate().isBefore(LocalDate.now())
                && !item.getExpirationDate().isAfter(LocalDate.now().plusDays(30));
    }

    private boolean containsIgnoreCase(String value, String query) {
        return query == null || query.isBlank()
                || (value != null && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)));
    }

    private boolean equalsIfPresent(String value, String expected) {
        return expected == null || expected.isBlank() || expected.equalsIgnoreCase(value);
    }

    private void validateFolder(String userCode, String folderCode) {
        if (folderCode != null && !folderCode.isBlank()
                && !folderRepository.existsByFolderCodeAndUserCode(folderCode, userCode)) {
            throw VaultException.badRequest("Folder is invalid");
        }
    }

    private VaultItemResponse toResponse(VaultItem item, boolean includeSensitive) {
        return new VaultItemResponse(
                item.getVaultItemCode(),
                item.getFolderCode(),
                item.getTitle(),
                item.getType(),
                item.getCategory(),
                item.getSecurityLevel(),
                item.getDescription(),
                readJson(item.getMetadataJson()),
                includeSensitive ? readSensitive(item) : null,
                item.getExpirationDate(),
                item.getStatus(),
                item.getFavorite(),
                vaultItemTagRepository.findByIdVaultItemCode(item.getVaultItemCode()).stream()
                        .map(tag -> tag.getId().getTagCode())
                        .toList(),
                item.getCreateDate(),
                item.getUpdateDate()
        );
    }

    private Map<String, Object> readSensitive(VaultItem item) {
        if (item.getEncryptedContent() == null) {
            return null;
        }
        return readJson(encryptionService.decryptString(item.getEncryptedContent(), item.getEncryptionIv()));
    }

    private boolean requiresExtraProtection(VaultItem item) {
        return AppConstants.SECURITY_SECRET.equalsIgnoreCase(item.getSecurityLevel())
                || AppConstants.SECURITY_CRITICAL.equalsIgnoreCase(item.getSecurityLevel());
    }

    private String writeJson(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw VaultException.badRequest("JSON content is invalid");
        }
    }

    private Map<String, Object> readJson(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, MAP_TYPE);
        } catch (Exception exception) {
            return null;
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
