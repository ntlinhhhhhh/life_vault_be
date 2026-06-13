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
        name = "folders",
        indexes = {
                @Index(name = "idx_folders_user_parent", columnList = "user_code,parent_folder_code"),
                @Index(name = "idx_folders_user_name", columnList = "user_code,name"),
                @Index(name = "idx_folders_workflow_state", columnList = "workflow_state")
        }
)
public class Folder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "folder_code", length = 100, nullable = false, unique = true)
    private String folderCode;

    @Column(name = "user_code", length = 100, nullable = false)
    private String userCode;

    @Column(name = "parent_folder_code", length = 100)
    private String parentFolderCode;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "path", length = 1000)
    private String path;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
