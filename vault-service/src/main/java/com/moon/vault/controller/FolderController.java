package com.moon.vault.controller;

import com.moon.vault.common.ResultResp;
import com.moon.vault.config.OpenApiConfig;
import com.moon.vault.dto.request.CreateFolderRequest;
import com.moon.vault.dto.request.UpdateFolderRequest;
import com.moon.vault.dto.response.FolderResponse;
import com.moon.vault.security.CurrentUserService;
import com.moon.vault.service.FolderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vault/folders")
@RequiredArgsConstructor
@Tag(name = "Folders", description = "Folder CRUD APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class FolderController {

    private final CurrentUserService currentUserService;
    private final FolderService folderService;

    @GetMapping
    public ResultResp<List<FolderResponse>> list(HttpServletRequest request) {
        return ResultResp.success(folderService.list(currentUserService.userCode(request)));
    }

    @PostMapping
    public ResultResp<FolderResponse> create(@Valid @RequestBody CreateFolderRequest body, HttpServletRequest request) {
        return ResultResp.success("Folder created", folderService.create(currentUserService.userCode(request), body, request));
    }

    @PutMapping("/{folderCode}")
    public ResultResp<FolderResponse> update(
            @PathVariable String folderCode,
            @Valid @RequestBody UpdateFolderRequest body,
            HttpServletRequest request
    ) {
        return ResultResp.success(folderService.update(currentUserService.userCode(request), folderCode, body, request));
    }

    @DeleteMapping("/{folderCode}")
    public ResultResp<Void> delete(@PathVariable String folderCode, HttpServletRequest request) {
        folderService.delete(currentUserService.userCode(request), folderCode, request);
        return ResultResp.success("Folder deleted", null);
    }
}
