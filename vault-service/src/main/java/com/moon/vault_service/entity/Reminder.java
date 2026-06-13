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

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "reminders",
        indexes = {
                @Index(name = "idx_reminders_user_status", columnList = "user_code,status"),
                @Index(name = "idx_reminders_item_status", columnList = "vault_item_code,status"),
                @Index(name = "idx_reminders_remind_at", columnList = "remind_at")
        }
)
public class Reminder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reminder_code", length = 100, nullable = false, unique = true)
    private String reminderCode;

    @Column(name = "vault_item_code", length = 100, nullable = false)
    private String vaultItemCode;

    @Column(name = "user_code", length = 100, nullable = false)
    private String userCode;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Column(name = "reminder_type", length = 50, nullable = false)
    private String reminderType;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "PENDING";

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
