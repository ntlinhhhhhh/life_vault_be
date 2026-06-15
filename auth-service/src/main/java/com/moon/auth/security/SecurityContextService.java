package com.moon.auth.security;

import com.moon.auth.exception.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextService {

    public SecurityUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new AuthException("UNAUTHORIZED", "Authentication is required", HttpStatus.UNAUTHORIZED);
        }
        return securityUser;
    }

    public String currentUserCode() {
        return currentUser().getUserCode();
    }
}

