package com.moon.auth.service.impl;

import com.moon.auth.common.constant.AppConstants;
import com.moon.auth.common.exception.ErrorCode;
import com.moon.auth.common.exception.ErrorMessage;
import com.moon.auth.dto.response.SessionResponse;
import com.moon.auth.entity.UserSession;
import com.moon.auth.exception.AuthException;
import com.moon.auth.mapper.SessionMapper;
import com.moon.auth.repository.UserSessionRepository;
import com.moon.auth.security.SecurityUser;
import com.moon.auth.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final UserSessionRepository userSessionRepository;
    private final SessionMapper sessionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SessionResponse> listSessions(SecurityUser securityUser) {
        return userSessionRepository.findByUserCodeOrderByCreateDateDesc(securityUser.getUserCode()).stream()
                .map(sessionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void revokeSession(SecurityUser securityUser, String sessionCode) {
        UserSession session = userSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> AuthException.notFound(ErrorCode.SESSION_NOT_FOUND, ErrorMessage.SESSION_NOT_FOUND));
        if (!securityUser.getUserCode().equals(session.getUserCode())) {
            throw AuthException.forbidden(ErrorCode.SESSION_FORBIDDEN, ErrorMessage.SESSION_FORBIDDEN);
        }
        session.setStatus(AppConstants.STATUS_REVOKED);
        session.setRevokedAt(LocalDateTime.now());
        session.setUpdateUser(securityUser.getUserCode());
        userSessionRepository.save(session);
    }
}
