package com.moon.auth.mapper;

import com.moon.auth.dto.response.SessionResponse;
import com.moon.auth.entity.UserSession;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionResponse toResponse(UserSession session) {
        return new SessionResponse(
                session.getSessionCode(),
                session.getDeviceId(),
                session.getDeviceName(),
                session.getIpAddress(),
                session.getStatus(),
                session.getExpiresAt(),
                session.getRevokedAt(),
                session.getCreateDate()
        );
    }
}
