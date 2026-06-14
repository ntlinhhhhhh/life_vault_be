package com.moon.auth.service;

import com.moon.auth.dto.request.UpdateProfileRequest;
import com.moon.auth.dto.response.UserInfoResponse;
import com.moon.auth.security.SecurityUser;

public interface UserService {

    UserInfoResponse me(SecurityUser securityUser);

    UserInfoResponse updateProfile(UpdateProfileRequest request, SecurityUser securityUser);
}
