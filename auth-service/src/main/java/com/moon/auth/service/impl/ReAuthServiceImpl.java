package com.moon.auth.service.impl;

import com.moon.auth.common.exception.ErrorCode;
import com.moon.auth.common.exception.ErrorMessage;
import com.moon.auth.dto.request.ReAuthRequest;
import com.moon.auth.dto.response.ReAuthResponse;
import com.moon.auth.entity.User;
import com.moon.auth.exception.AuthException;
import com.moon.auth.repository.UserRepository;
import com.moon.auth.security.JwtService;
import com.moon.auth.security.SecurityUser;
import com.moon.auth.service.ReAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReAuthServiceImpl implements ReAuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public ReAuthResponse reAuth(ReAuthRequest request, SecurityUser securityUser) {
        User user = userRepository.findByUserCode(securityUser.getUserCode())
                .orElseThrow(() -> AuthException.notFound(ErrorCode.USER_NOT_FOUND, ErrorMessage.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw AuthException.unauthorized(ErrorCode.INVALID_PASSWORD, ErrorMessage.INVALID_PASSWORD);
        }
        return new ReAuthResponse(jwtService.generateReauthToken(user), jwtService.getReauthTokenTtlSeconds());
    }
}
