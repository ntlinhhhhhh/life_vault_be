package com.moon.auth.service.impl;

import com.moon.auth.common.exception.ErrorCode;
import com.moon.auth.common.exception.ErrorMessage;
import com.moon.auth.common.util.StringUtils;
import com.moon.auth.dto.request.UpdateProfileRequest;
import com.moon.auth.dto.response.UserInfoResponse;
import com.moon.auth.entity.User;
import com.moon.auth.exception.AuthException;
import com.moon.auth.mapper.UserMapper;
import com.moon.auth.repository.UserRepository;
import com.moon.auth.security.SecurityUser;
import com.moon.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse me(SecurityUser securityUser) {
        return userMapper.toUserInfoResponse(securityUser.getUser());
    }

    @Override
    @Transactional
    public UserInfoResponse updateProfile(UpdateProfileRequest request, SecurityUser securityUser) {
        User user = userRepository.findByUserCode(securityUser.getUserCode())
                .orElseThrow(() -> AuthException.notFound(ErrorCode.USER_NOT_FOUND, ErrorMessage.USER_NOT_FOUND));

        String username = StringUtils.trimToNull(request.getUsername());
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw AuthException.badRequest(ErrorCode.USERNAME_EXISTS, ErrorMessage.USERNAME_EXISTS);
            }
            user.setUsername(username);
        }

        String email = StringUtils.trimToNull(request.getEmail());
        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw AuthException.badRequest(ErrorCode.EMAIL_EXISTS, ErrorMessage.EMAIL_EXISTS);
            }
            user.setEmail(email);
        }

        user.setUpdateUser(securityUser.getUserCode());
        return userMapper.toUserInfoResponse(userRepository.save(user));
    }
}
