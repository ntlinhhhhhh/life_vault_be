package com.moon.auth.service;

import com.moon.auth.dto.request.ReAuthRequest;
import com.moon.auth.dto.response.ReAuthResponse;
import com.moon.auth.security.SecurityUser;

public interface ReAuthService {

    ReAuthResponse reAuth(ReAuthRequest request, SecurityUser securityUser);
}
