package com.moon.vault.controller;

import com.moon.vault.common.ResultResp;
import com.moon.vault.config.OpenApiConfig;
import com.moon.vault.dto.request.CreateTagRequest;
import com.moon.vault.dto.request.UpdateTagRequest;
import com.moon.vault.dto.response.TagResponse;
import com.moon.vault.security.CurrentUserService;
import com.moon.vault.service.TagService;
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
@RequestMapping("/vault/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Tag CRUD APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class TagController {

    private final CurrentUserService currentUserService;
    private final TagService tagService;

    @GetMapping
    public ResultResp<List<TagResponse>> list(HttpServletRequest request) {
        return ResultResp.success(tagService.list(currentUserService.userCode(request)));
    }

    @PostMapping
    public ResultResp<TagResponse> create(@Valid @RequestBody CreateTagRequest body, HttpServletRequest request) {
        return ResultResp.success("Tag created", tagService.create(currentUserService.userCode(request), body, request));
    }

    @PutMapping("/{tagCode}")
    public ResultResp<TagResponse> update(
            @PathVariable String tagCode,
            @Valid @RequestBody UpdateTagRequest body,
            HttpServletRequest request
    ) {
        return ResultResp.success(tagService.update(currentUserService.userCode(request), tagCode, body, request));
    }

    @DeleteMapping("/{tagCode}")
    public ResultResp<Void> delete(@PathVariable String tagCode, HttpServletRequest request) {
        tagService.delete(currentUserService.userCode(request), tagCode, request);
        return ResultResp.success("Tag deleted", null);
    }
}
