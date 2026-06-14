package com.moon.vault.service.impl;

import com.moon.vault.common.AppConstants;
import com.moon.vault.dto.request.CreateTagRequest;
import com.moon.vault.dto.request.UpdateTagRequest;
import com.moon.vault.dto.response.TagResponse;
import com.moon.vault.entity.Tag;
import com.moon.vault.exception.VaultException;
import com.moon.vault.repository.TagRepository;
import com.moon.vault.repository.VaultItemTagRepository;
import com.moon.vault.service.AuditLogService;
import com.moon.vault.service.TagService;
import com.moon.vault.util.CodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final VaultItemTagRepository vaultItemTagRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> list(String userCode) {
        return tagRepository.findByUserCodeOrderByNameAsc(userCode).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public TagResponse create(String userCode, CreateTagRequest request, HttpServletRequest httpRequest) {
        if (tagRepository.existsByNameAndUserCode(request.name(), userCode)) {
            throw VaultException.badRequest("Tag name already exists");
        }
        Tag tag = new Tag();
        tag.setTagCode(CodeGenerator.generate(AppConstants.TAG_CODE_PREFIX));
        tag.setUserCode(userCode);
        tag.setName(request.name());
        tag.setColor(request.color());
        tag.setCreateUser(userCode);
        tag = tagRepository.save(tag);
        auditLogService.record(userCode, "CREATE_TAG", "TAG", tag.getTagCode(), httpRequest);
        return toResponse(tag);
    }

    @Override
    @Transactional
    public TagResponse update(String userCode, String tagCode, UpdateTagRequest request, HttpServletRequest httpRequest) {
        Tag tag = getTag(userCode, tagCode);
        tag.setName(request.name());
        tag.setColor(request.color());
        tag.setUpdateUser(userCode);
        auditLogService.record(userCode, "UPDATE_TAG", "TAG", tagCode, httpRequest);
        return toResponse(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public void delete(String userCode, String tagCode, HttpServletRequest httpRequest) {
        Tag tag = getTag(userCode, tagCode);
        vaultItemTagRepository.deleteByIdTagCode(tagCode);
        tagRepository.delete(tag);
        auditLogService.record(userCode, "DELETE_TAG", "TAG", tagCode, httpRequest);
    }

    private Tag getTag(String userCode, String tagCode) {
        return tagRepository.findByTagCodeAndUserCode(tagCode, userCode)
                .orElseThrow(() -> VaultException.notFound("Tag not found"));
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getTagCode(), tag.getName(), tag.getColor());
    }
}
