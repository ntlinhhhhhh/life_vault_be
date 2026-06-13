package com.moon.auth_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "login_logs",
        indexes = {
                @Index(name = "idx_login_logs_user_date", columnList = "user_code,create_date"),
                @Index(name = "idx_login_logs_success_date", columnList = "success,create_date")
        }
)
public class LoginLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_log_code", length = 100, nullable = false, unique = true)
    private String loginLogCode;

    @Column(name = "user_code", length = 100)
    private String userCode;

    @Column(name = "username_input", length = 255, nullable = false)
    private String usernameInput;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
}
