package com.moon.auth.security;

import com.moon.auth.common.constant.AppConstants;
import com.moon.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SecurityUser implements UserDetails {

    private final User user;
    private final List<String> roles;

    public SecurityUser(User user, List<String> roles) {
        this.user = user;
        this.roles = roles;
    }

    public String getUserCode() {
        return user.getUserCode();
    }

    public User getUser() {
        return user;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUserCode();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !AppConstants.STATUS_LOCKED.equalsIgnoreCase(user.getStatus());
    }

    @Override
    public boolean isEnabled() {
        return AppConstants.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus());
    }
}
