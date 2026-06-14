package com.moon.vault.service.impl;

import com.moon.vault.common.AppConstants;
import com.moon.vault.dto.request.CreateFolderRequest;
import com.moon.vault.dto.request.UpdateFolderRequest;
import com.moon.vault.dto.response.FolderResponse;
import com.moon.vault.entity.Folder;
import com.moon.vault.exception.VaultException;
import com.moon.vault.repository.FolderRepository;
import com.moon.vault.repository.VaultItemRepository;
import com.moon.vault.service.AuditLogService;
import com.moon.vault.service.FolderService;
import com.moon.vault.util.CodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    private final FolderRepository folderRepository;
    private final VaultItemRepository vaultItemRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public List<FolderResponse> list(String userCode) {
        return folderRepository.findByUserCodeAndWorkflowStateOrderBySortOrderAscCreateDateAsc(userCode, "active")
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public FolderResponse create(String userCode, CreateFolderRequest request, HttpServletRequest httpRequest) {
        Folder folder = new Folder();
        folder.setFolderCode(CodeGenerator.generate(AppConstants.FOLDER_CODE_PREFIX));
        folder.setUserCode(userCode);
        folder.setParentFolderCode(request.parentFolderCode());
        folder.setName(request.name());
        folder.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        folder.setPath(path(userCode, request.parentFolderCode(), request.name()));
        folder.setCreateUser(userCode);
        folder = folderRepository.save(folder);
        auditLogService.record(userCode, "CREATE_FOLDER", "FOLDER", folder.getFolderCode(), httpRequest);
        return toResponse(folder);
    }

    @Override
    @Transactional
    public FolderResponse update(String userCode, String folderCode, UpdateFolderRequest request, HttpServletRequest httpRequest) {
        Folder folder = getFolder(userCode, folderCode);
        folder.setName(request.name());
        folder.setParentFolderCode(request.parentFolderCode());
        folder.setSortOrder(request.sortOrder() == null ? folder.getSortOrder() : request.sortOrder());
        folder.setPath(path(userCode, request.parentFolderCode(), request.name()));
        folder.setUpdateUser(userCode);
        auditLogService.record(userCode, "UPDATE_FOLDER", "FOLDER", folderCode, httpRequest);
        return toResponse(folderRepository.save(folder));
    }

    @Override
    @Transactional
    public void delete(String userCode, String folderCode, HttpServletRequest httpRequest) {
        Folder folder = getFolder(userCode, folderCode);
        if (vaultItemRepository.existsByFolderCodeAndUserCodeAndWorkflowState(folderCode, userCode, "active")) {
            throw VaultException.badRequest("Folder still contains active items");
        }
        folder.setWorkflowState("deleted");
        folder.setDeletedAt(LocalDateTime.now());
        folder.setUpdateUser(userCode);
        folderRepository.save(folder);
        auditLogService.record(userCode, "DELETE_FOLDER", "FOLDER", folderCode, httpRequest);
    }

    private Folder getFolder(String userCode, String folderCode) {
        return folderRepository.findByFolderCodeAndUserCode(folderCode, userCode)
                .filter(folder -> !"deleted".equalsIgnoreCase(folder.getWorkflowState()))
                .orElseThrow(() -> VaultException.notFound("Folder not found"));
    }

    private String path(String userCode, String parentFolderCode, String name) {
        if (parentFolderCode == null || parentFolderCode.isBlank()) {
            return "/" + name;
        }
        Folder parent = getFolder(userCode, parentFolderCode);
        return parent.getPath() + "/" + name;
    }

    private FolderResponse toResponse(Folder folder) {
        return new FolderResponse(
                folder.getFolderCode(),
                folder.getParentFolderCode(),
                folder.getName(),
                folder.getPath(),
                folder.getSortOrder(),
                folder.getCreateDate(),
                folder.getUpdateDate()
        );
    }
}
