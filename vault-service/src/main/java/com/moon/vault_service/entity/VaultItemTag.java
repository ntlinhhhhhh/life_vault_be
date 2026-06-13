package com.moon.vault_service.entity;

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
        name = "vault_item_tags",
        indexes = {
                @Index(name = "idx_vault_item_tags_tag_code", columnList = "tag_code")
        }
)
public class VaultItemTag extends BaseEntity {

    @EmbeddedId
    private VaultItemTagId id = new VaultItemTagId();
}
