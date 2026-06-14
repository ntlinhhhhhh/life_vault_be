package com.moon.vault.service;

import com.moon.vault.dto.request.CreatePasswordRequest;
import com.moon.vault.dto.request.UpdatePasswordRequest;
import com.moon.vault.dto.response.PasswordDetailResponse;
import com.moon.vault.dto.response.PasswordRevealResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PasswordVaultService {

    PasswordDetailResponse create(String userCode, CreatePasswordRequest request, HttpServletRequest httpRequest);

    PasswordDetailResponse detail(String userCode, String itemCode, HttpServletRequest httpRequest);

    PasswordRevealResponse reveal(String userCode, String itemCode, String reauthToken, HttpServletRequest httpRequest);

    PasswordDetailResponse update(String userCode, String itemCode, UpdatePasswordRequest request, HttpServletRequest httpRequest);

    PasswordRevealResponse copy(String userCode, String itemCode, String reauthToken, HttpServletRequest httpRequest);
}
