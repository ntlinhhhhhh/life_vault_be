package com.moon.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SessionResponse {

    private String sessionCode;
    private String deviceId;
    private String deviceName;
    private String ipAddress;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;
    private LocalDateTime createDate;
}

