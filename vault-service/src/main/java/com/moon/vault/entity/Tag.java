package com.moon.vault.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "tags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tags_user_name", columnNames = {"user_code", "name"})
        },
        indexes = {
                @Index(name = "idx_tags_user_code", columnList = "user_code")
        }
)
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_code", length = 100, nullable = false, unique = true)
    private String tagCode;

    @Column(name = "user_code", length = 100, nullable = false)
    private String userCode;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "color", length = 20)
    private String color;
}
