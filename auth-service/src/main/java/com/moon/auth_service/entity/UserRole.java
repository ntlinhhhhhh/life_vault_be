package com.moon.auth_service.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
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
        name = "user_roles",
        indexes = {
                @Index(name = "idx_user_roles_role_code", columnList = "role_code")
        }
)
public class UserRole extends BaseEntity {

    @EmbeddedId
    private UserRoleId id = new UserRoleId();
}
