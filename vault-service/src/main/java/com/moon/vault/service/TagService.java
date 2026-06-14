package com.moon.vault.service;

import com.moon.vault.dto.request.CreateTagRequest;
import com.moon.vault.dto.request.UpdateTagRequest;
import com.moon.vault.dto.response.TagResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface TagService {

    List<TagResponse> list(String userCode);

    TagResponse create(String userCode, CreateTagRequest request, HttpServletRequest httpRequest);

    TagResponse update(String userCode, String tagCode, UpdateTagRequest request, HttpServletRequest httpRequest);

    void delete(String userCode, String tagCode, HttpServletRequest httpRequest);
}
