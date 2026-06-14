package com.moon.auth.security;

import com.moon.auth.common.constant.AuthoritiesConstants;
import com.moon.auth.entity.User;
import com.moon.auth.repository.UserRepository;
import com.moon.auth.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String userCode) throws UsernameNotFoundException {
        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<String> roles = userRoleRepository.findByIdUserCode(userCode).stream()
                .map(userRole -> userRole.getId().getRoleCode())
                .toList();
        if (roles.isEmpty()) {
            roles = List.of(AuthoritiesConstants.USER);
        }
        return new SecurityUser(user, roles);
    }
}
