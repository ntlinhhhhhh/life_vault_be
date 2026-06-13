package com.moon.vault.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class
BaseEntity {

    @Column(name = "create_user", length = 100)
    private String createUser;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "update_user", length = 100)
    private String updateUser;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @Column(name = "workflow_state", length = 100, nullable = false)
    private String workflowState = "active";

    @Column(name = "sync_source", length = 300)
    private String syncSource;

    @PrePersist
    protected void prePersist() {
        if (createDate == null) {
            createDate = LocalDateTime.now();
        }
        if (workflowState == null) {
            workflowState = "active";
        }
    }

    @PreUpdate
    protected void preUpdate() {
        updateDate = LocalDateTime.now();
    }
}
