package com.moon.vault_service.entity;

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
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_user_date", columnList = "user_code,create_date"),
                @Index(name = "idx_audit_logs_action_date", columnList = "action_type,create_date"),
                @Index(name = "idx_audit_logs_target", columnList = "target_type,target_code")
        }
)
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "audit_log_code", length = 100, nullable = false, unique = true)
    private String auditLogCode;

    @Column(name = "user_code", length = 100, nullable = false)
    private String userCode;

    @Column(name = "action_type", length = 100, nullable = false)
    private String actionType;

    @Column(name = "target_type", length = 100, nullable = false)
    private String targetType;

    @Column(name = "target_code", length = 100)
    private String targetCode;

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;
}
