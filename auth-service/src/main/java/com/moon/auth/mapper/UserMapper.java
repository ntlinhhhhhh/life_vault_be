package com.moon.auth.mapper;

import com.moon.auth.dto.response.RegisterResponse;
import com.moon.auth.dto.response.UserInfoResponse;
import com.moon.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public RegisterResponse toRegisterResponse(User user) {
        return new RegisterResponse(user.getUserCode(), user.getUsername(), user.getEmail());
    }

    public UserInfoResponse toUserInfoResponse(User user) {
        return new UserInfoResponse(user.getUserCode(), user.getUsername(), user.getEmail(), user.getStatus());
    }
}

