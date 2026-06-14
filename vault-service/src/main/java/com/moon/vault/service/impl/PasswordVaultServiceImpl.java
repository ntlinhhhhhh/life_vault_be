package com.moon.vault.service.impl;

import com.moon.vault.common.AppConstants;
import com.moon.vault.dto.request.CreatePasswordRequest;
import com.moon.vault.dto.request.UpdatePasswordRequest;
import com.moon.vault.dto.response.PasswordDetailResponse;
import com.moon.vault.dto.response.PasswordRevealResponse;
import com.moon.vault.entity.PasswordEntry;
import com.moon.vault.entity.Tag;
import com.moon.vault.entity.VaultItem;
import com.moon.vault.entity.VaultItemTag;
import com.moon.vault.entity.VaultItemTagId;
import com.moon.vault.exception.VaultException;
import com.moon.vault.repository.FolderRepository;
import com.moon.vault.repository.PasswordEntryRepository;
import com.moon.vault.repository.TagRepository;
import com.moon.vault.repository.VaultItemRepository;
import com.moon.vault.repository.VaultItemTagRepository;
import com.moon.vault.security.JwtService;
import com.moon.vault.service.AuditLogService;
import com.moon.vault.service.EncryptionService;
import com.moon.vault.service.PasswordVaultService;
import com.moon.vault.util.CodeGenerator;
import com.moon.vault.util.MaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordVaultServiceImpl implements PasswordVaultService {

    private final VaultItemRepository vaultItemRepository;
    private final PasswordEntryRepository passwordEntryRepository;
    private final VaultItemTagRepository vaultItemTagRepository;
    private final TagRepository tagRepository;
    private final FolderRepository folderRepository;
    private final EncryptionService encryptionService;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public PasswordDetailResponse create(String userCode, CreatePasswordRequest request, HttpServletRequest httpRequest) {
        validateFolder(userCode, request.folderCode());
        VaultItem item = new VaultItem();
        item.setVaultItemCode(CodeGenerator.generate(AppConstants.ITEM_CODE_PREFIX));
        item.setUserCode(userCode);
        item.setTitle(request.title());
        item.setFolderCode(blankToNull(request.folderCode()));
        item.setType(AppConstants.TYPE_PASSWORD_ENTRY);
        item.setCategory(AppConstants.CATEGORY_PASSWORD);
        item.setSecurityLevel(AppConstants.SECURITY_CRITICAL);
        item.setStatus(AppConstants.STATUS_ACTIVE);
        item.setFavorite(false);
        item.setCreateUser(userCode);
        item = vaultItemRepository.save(item);

        PasswordEntry entry = new PasswordEntry();
        entry.setPasswordEntryCode(CodeGenerator.generate(AppConstants.PASSWORD_CODE_PREFIX));
        entry.setVaultItemCode(item.getVaultItemCode());
        apply(entry, request.serviceName(), request.loginUrl(), request.username(), request.password(), request.note(), true);
        entry.setCreateUser(userCode);
        entry = passwordEntryRepository.save(entry);
        assignTags(userCode, item.getVaultItemCode(), request.tagCodes());
        auditLogService.record(userCode, "CREATE_PASSWORD", "PASSWORD", item.getVaultItemCode(), httpRequest);
        return toDetail(item, entry);
    }

    @Override
    @Transactional
    public PasswordDetailResponse detail(String userCode, String itemCode, HttpServletRequest httpRequest) {
        VaultItem item = getItem(userCode, itemCode);
        PasswordEntry entry = getEntry(itemCode);
        auditLogService.record(userCode, "VIEW_PASSWORD", "PASSWORD", itemCode, httpRequest);
        return toDetail(item, entry);
    }

    @Override
    @Transactional
    public PasswordRevealResponse reveal(String userCode, String itemCode, String reauthToken, HttpServletRequest httpRequest) {
        jwtService.validateReauthToken(reauthToken, userCode);
        getItem(userCode, itemCode);
        PasswordRevealResponse response = revealEntry(getEntry(itemCode));
        auditLogService.record(userCode, "REVEAL_PASSWORD", "PASSWORD", itemCode, httpRequest);
        return response;
    }

    @Override
    @Transactional
    public PasswordDetailResponse update(String userCode, String itemCode, UpdatePasswordRequest request, HttpServletRequest httpRequest) {
        validateFolder(userCode, request.folderCode());
        VaultItem item = getItem(userCode, itemCode);
        item.setTitle(request.title());
        item.setFolderCode(blankToNull(request.folderCode()));
        item.setUpdateUser(userCode);
        item = vaultItemRepository.save(item);

        PasswordEntry entry = getEntry(itemCode);
        apply(entry, request.serviceName(), request.loginUrl(), request.username(), request.password(), request.note(), request.password() != null);
        entry.setUpdateUser(userCode);
        entry = passwordEntryRepository.save(entry);
        assignTags(userCode, itemCode, request.tagCodes());
        auditLogService.record(userCode, "UPDATE_PASSWORD", "PASSWORD", itemCode, httpRequest);
        return toDetail(item, entry);
    }

    @Override
    @Transactional
    public PasswordRevealResponse copy(String userCode, String itemCode, String reauthToken, HttpServletRequest httpRequest) {
        jwtService.validateReauthToken(reauthToken, userCode);
        getItem(userCode, itemCode);
        PasswordRevealResponse response = revealEntry(getEntry(itemCode));
        auditLogService.record(userCode, "COPY_PASSWORD", "PASSWORD", itemCode, httpRequest);
        return response;
    }

    private void apply(
            PasswordEntry entry,
            String serviceName,
            String loginUrl,
            String username,
            String password,
            String note,
            boolean passwordChanged
    ) {
        entry.setServiceName(serviceName);
        entry.setLoginUrl(loginUrl);
        var encryptedUsername = encryptionService.encryptString(username);
        entry.setUsernameEncrypted(encryptedUsername.ciphertext());
        entry.setUsernameIv(encryptedUsername.iv());
        if (password != null) {
            var encryptedPassword = encryptionService.encryptString(password);
            entry.setPasswordEncrypted(encryptedPassword.ciphertext());
            entry.setPasswordIv(encryptedPassword.iv());
        }
        var encryptedNote = encryptionService.encryptString(note);
        entry.setNoteEncrypted(encryptedNote.ciphertext());
        entry.setNoteIv(encryptedNote.iv());
        if (passwordChanged) {
            entry.setLastChangedAt(LocalDateTime.now());
        }
    }

    private PasswordDetailResponse toDetail(VaultItem item, PasswordEntry entry) {
        String username = encryptionService.decryptString(entry.getUsernameEncrypted(), entry.getUsernameIv());
        return new PasswordDetailResponse(
                item.getVaultItemCode(),
                entry.getPasswordEntryCode(),
                entry.getServiceName(),
                entry.getLoginUrl(),
                MaskingUtil.maskText(username),
                MaskingUtil.maskPassword(),
                item.getSecurityLevel()
        );
    }

    private PasswordRevealResponse revealEntry(PasswordEntry entry) {
        return new PasswordRevealResponse(
                encryptionService.decryptString(entry.getUsernameEncrypted(), entry.getUsernameIv()),
                encryptionService.decryptString(entry.getPasswordEncrypted(), entry.getPasswordIv()),
                encryptionService.decryptString(entry.getNoteEncrypted(), entry.getNoteIv())
        );
    }

    private VaultItem getItem(String userCode, String itemCode) {
        return vaultItemRepository.findByVaultItemCodeAndUserCode(itemCode, userCode)
                .filter(item -> AppConstants.TYPE_PASSWORD_ENTRY.equalsIgnoreCase(item.getType()))
                .filter(item -> !"deleted".equalsIgnoreCase(item.getWorkflowState()))
                .orElseThrow(() -> VaultException.notFound("Password item not found"));
    }

    private PasswordEntry getEntry(String itemCode) {
        return passwordEntryRepository.findByVaultItemCode(itemCode)
                .orElseThrow(() -> VaultException.notFound("Password entry not found"));
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

    private void validateFolder(String userCode, String folderCode) {
        if (folderCode != null && !folderCode.isBlank()
                && !folderRepository.existsByFolderCodeAndUserCode(folderCode, userCode)) {
            throw VaultException.badRequest("Folder is invalid");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
