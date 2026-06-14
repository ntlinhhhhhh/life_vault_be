package com.moon.vault.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.moon.vault.common.AppConstants.AUTHORIZATION_HEADER;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final JwtService jwtService;

    public String userCode(HttpServletRequest request) {
        return jwtService.subjectFromAccessToken(request.getHeader(AUTHORIZATION_HEADER));
    }
}
