package com.moon.auth.service;

import com.moon.auth.dto.response.SessionResponse;
import com.moon.auth.security.SecurityUser;

import java.util.List;

public interface SessionService {

    List<SessionResponse> listSessions(SecurityUser securityUser);

    void revokeSession(SecurityUser securityUser, String sessionCode);
}

