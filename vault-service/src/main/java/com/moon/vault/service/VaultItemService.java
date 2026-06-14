package com.moon.vault.service;

import com.moon.vault.common.PageResp;
import com.moon.vault.dto.request.CreateVaultItemRequest;
import com.moon.vault.dto.request.RenewItemRequest;
import com.moon.vault.dto.request.SearchVaultItemRequest;
import com.moon.vault.dto.request.UpdateVaultItemRequest;
import com.moon.vault.dto.response.VaultItemResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface VaultItemService {

    PageResp<VaultItemResponse> search(String userCode, SearchVaultItemRequest request);

    VaultItemResponse create(String userCode, CreateVaultItemRequest request, HttpServletRequest httpRequest);

    VaultItemResponse detail(String userCode, String itemCode, HttpServletRequest httpRequest);

    VaultItemResponse update(String userCode, String itemCode, UpdateVaultItemRequest request, HttpServletRequest httpRequest);

    void delete(String userCode, String itemCode, HttpServletRequest httpRequest);

    VaultItemResponse restore(String userCode, String itemCode, HttpServletRequest httpRequest);

    void purge(String userCode, String itemCode, String reauthToken, HttpServletRequest httpRequest);

    VaultItemResponse toggleFavorite(String userCode, String itemCode, HttpServletRequest httpRequest);

    VaultItemResponse archive(String userCode, String itemCode, HttpServletRequest httpRequest);

    VaultItemResponse renew(String userCode, String itemCode, RenewItemRequest request, HttpServletRequest httpRequest);
}
