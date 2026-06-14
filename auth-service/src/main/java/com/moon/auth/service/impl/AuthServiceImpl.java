package com.moon.auth.service.impl;

import com.moon.auth.common.constant.AppConstants;
import com.moon.auth.common.constant.AuthoritiesConstants;
import com.moon.auth.common.exception.ErrorCode;
import com.moon.auth.common.exception.ErrorMessage;
import com.moon.auth.common.util.TokenUtils;
import com.moon.auth.dto.request.ChangePasswordRequest;
import com.moon.auth.dto.request.LoginRequest;
import com.moon.auth.dto.request.LogoutRequest;
import com.moon.auth.dto.request.RefreshTokenRequest;
import com.moon.auth.dto.request.RegisterRequest;
import com.moon.auth.dto.response.LoginResponse;
import com.moon.auth.dto.response.RegisterResponse;
import com.moon.auth.dto.response.TokenResponse;
import com.moon.auth.dto.response.UserInfoResponse;
import com.moon.auth.entity.Role;
import com.moon.auth.entity.User;
import com.moon.auth.entity.UserRole;
import com.moon.auth.entity.UserRoleId;
import com.moon.auth.entity.UserSession;
import com.moon.auth.exception.AuthException;
import com.moon.auth.mapper.UserMapper;
import com.moon.auth.repository.RoleRepository;
import com.moon.auth.repository.UserRepository;
import com.moon.auth.repository.UserRoleRepository;
import com.moon.auth.repository.UserSessionRepository;
import com.moon.auth.security.JwtService;
import com.moon.auth.security.SecurityUser;
import com.moon.auth.service.AuthService;
import com.moon.auth.service.LoginTrackingService;
import com.moon.auth.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RedisService redisService;
    private final LoginTrackingService loginTrackingService;

    @Value("${auth.refresh-token-ttl-seconds}")
    private long refreshTokenTtlSeconds;

    @Value("${auth.max-failed-login-count}")
    private int maxFailedLoginCount;

    @Value("${auth.lock-duration-minutes}")
    private long lockDurationMinutes;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw AuthException.badRequest(ErrorCode.USERNAME_EXISTS, ErrorMessage.USERNAME_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw AuthException.badRequest(ErrorCode.EMAIL_EXISTS, ErrorMessage.EMAIL_EXISTS);
        }

        User user = new User();
        user.setUserCode(TokenUtils.generateCode(AppConstants.USER_CODE_PREFIX));
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(AppConstants.STATUS_ACTIVE);
        user.setFailedLoginCount(0);
        user.setCreateUser(AppConstants.SYSTEM_USER);
        user = userRepository.save(user);

        ensureUserRole(user.getUserCode());
        return userMapper.toRegisterResponse(user);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElse(null);
        if (user == null) {
            loginTrackingService.record(null, request.getUsernameOrEmail(), false, ErrorCode.USER_NOT_FOUND.name(), ipAddress, userAgent);
            throw AuthException.unauthorized(ErrorCode.INVALID_CREDENTIALS, ErrorMessage.INVALID_CREDENTIALS);
        }

        normalizeExpiredLock(user);
        if (!AppConstants.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            loginTrackingService.record(user.getUserCode(), request.getUsernameOrEmail(), false, ErrorCode.USER_NOT_ACTIVE.name(), ipAddress, userAgent);
            throw AuthException.forbidden(ErrorCode.USER_NOT_ACTIVE, ErrorMessage.USER_NOT_ACTIVE);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            loginTrackingService.record(user.getUserCode(), request.getUsernameOrEmail(), false, ErrorCode.INVALID_CREDENTIALS.name(), ipAddress, userAgent);
            throw AuthException.unauthorized(ErrorCode.INVALID_CREDENTIALS, ErrorMessage.INVALID_CREDENTIALS);
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdateUser(user.getUserCode());
        userRepository.save(user);

        List<String> roles = getRoles(user.getUserCode());
        String accessToken = jwtService.generateAccessToken(user, roles);
        String refreshToken = TokenUtils.createRefreshToken();
        saveSession(user, refreshToken, request, ipAddress, userAgent);
        loginTrackingService.record(user.getUserCode(), request.getUsernameOrEmail(), true, null, ipAddress, userAgent);

        UserInfoResponse userInfo = userMapper.toUserInfoResponse(user);
        return new LoginResponse(accessToken, refreshToken, jwtService.getAccessTokenTtlSeconds(), userInfo);
    }

    @Override
    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshTokenHash = TokenUtils.sha256(request.getRefreshToken());
        UserSession session = userSessionRepository.findByRefreshTokenHash(refreshTokenHash)
                .orElseThrow(() -> AuthException.unauthorized(ErrorCode.INVALID_REFRESH_TOKEN, ErrorMessage.INVALID_REFRESH_TOKEN));

        if (!AppConstants.STATUS_ACTIVE.equalsIgnoreCase(session.getStatus()) || session.getExpiresAt().isBefore(LocalDateTime.now())) {
            if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
                session.setStatus(AppConstants.STATUS_EXPIRED);
                userSessionRepository.save(session);
            }
            throw AuthException.unauthorized(ErrorCode.REFRESH_TOKEN_EXPIRED, ErrorMessage.REFRESH_TOKEN_EXPIRED);
        }

        User user = userRepository.findByUserCode(session.getUserCode())
                .orElseThrow(() -> AuthException.notFound(ErrorCode.USER_NOT_FOUND, ErrorMessage.USER_NOT_FOUND));
        if (!AppConstants.STATUS_ACTIVE.equalsIgnoreCase(user.getStatus())) {
            throw AuthException.forbidden(ErrorCode.USER_NOT_ACTIVE, ErrorMessage.USER_NOT_ACTIVE);
        }

        String accessToken = jwtService.generateAccessToken(user, getRoles(user.getUserCode()));
        return new TokenResponse(accessToken, jwtService.getAccessTokenTtlSeconds());
    }

    @Override
    @Transactional
    public void logout(LogoutRequest request, String accessToken) {
        String refreshTokenHash = TokenUtils.sha256(request.getRefreshToken());
        userSessionRepository.findByRefreshTokenHash(refreshTokenHash).ifPresent(session -> {
            session.setStatus(AppConstants.STATUS_REVOKED);
            session.setRevokedAt(LocalDateTime.now());
            session.setUpdateUser(session.getUserCode());
            userSessionRepository.save(session);
        });
        blacklistAccessToken(accessToken);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, SecurityUser securityUser) {
        User user = userRepository.findByUserCode(securityUser.getUserCode())
                .orElseThrow(() -> AuthException.notFound(ErrorCode.USER_NOT_FOUND, ErrorMessage.USER_NOT_FOUND));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw AuthException.unauthorized(ErrorCode.INVALID_OLD_PASSWORD, ErrorMessage.INVALID_OLD_PASSWORD);
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateUser(user.getUserCode());
        userRepository.save(user);
    }

    private void ensureUserRole(String userCode) {
        Role role = roleRepository.findByRoleCode(AuthoritiesConstants.USER).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRoleCode(AuthoritiesConstants.USER);
            newRole.setName("Standard User");
            newRole.setCreateUser(AppConstants.SYSTEM_USER);
            return roleRepository.save(newRole);
        });

        UserRoleId id = new UserRoleId();
        id.setUserCode(userCode);
        id.setRoleCode(role.getRoleCode());

        if (!userRoleRepository.existsById(id)) {
            UserRole userRole = new UserRole();
            userRole.setId(id);
            userRole.setCreateUser(AppConstants.SYSTEM_USER);
            userRoleRepository.save(userRole);
        }
    }

    private List<String> getRoles(String userCode) {
        List<String> roles = userRoleRepository.findByIdUserCode(userCode).stream()
                .map(userRole -> userRole.getId().getRoleCode())
                .toList();
        return roles.isEmpty() ? List.of(AuthoritiesConstants.USER) : roles;
    }

    private void saveSession(User user, String refreshToken, LoginRequest request, String ipAddress, String userAgent) {
        UserSession session = new UserSession();
        session.setSessionCode(TokenUtils.generateCode(AppConstants.SESSION_CODE_PREFIX));
        session.setUserCode(user.getUserCode());
        session.setRefreshTokenHash(TokenUtils.sha256(refreshToken));
        session.setDeviceId(request.getDeviceId());
        session.setDeviceName(request.getDeviceName());
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setStatus(AppConstants.STATUS_ACTIVE);
        session.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenTtlSeconds));
        session.setCreateUser(user.getUserCode());
        userSessionRepository.save(session);
    }

    private void handleFailedLogin(User user) {
        int failedCount = user.getFailedLoginCount() == null ? 1 : user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(failedCount);
        user.setUpdateUser(user.getUserCode());
        if (failedCount >= maxFailedLoginCount) {
            user.setStatus(AppConstants.STATUS_LOCKED);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
        }
        userRepository.save(user);
    }

    private void normalizeExpiredLock(User user) {
        if (AppConstants.STATUS_LOCKED.equalsIgnoreCase(user.getStatus())
                && user.getLockedUntil() != null
                && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setStatus(AppConstants.STATUS_ACTIVE);
            user.setLockedUntil(null);
            user.setFailedLoginCount(0);
            user.setUpdateUser(user.getUserCode());
            userRepository.save(user);
        }
    }

    private void blacklistAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank() || !jwtService.isValid(accessToken)) {
            return;
        }
        Instant expiration = jwtService.getExpiration(accessToken).toInstant();
        Duration ttl = Duration.between(Instant.now(), expiration);
        redisService.blacklistToken(accessToken, ttl);
    }
}
