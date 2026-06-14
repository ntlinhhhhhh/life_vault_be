package com.moon.vault.service;

import com.moon.vault.dto.request.CreateFolderRequest;
import com.moon.vault.dto.request.UpdateFolderRequest;
import com.moon.vault.dto.response.FolderResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface FolderService {

    List<FolderResponse> list(String userCode);

    FolderResponse create(String userCode, CreateFolderRequest request, HttpServletRequest httpRequest);

    FolderResponse update(String userCode, String folderCode, UpdateFolderRequest request, HttpServletRequest httpRequest);

    void delete(String userCode, String folderCode, HttpServletRequest httpRequest);
}
