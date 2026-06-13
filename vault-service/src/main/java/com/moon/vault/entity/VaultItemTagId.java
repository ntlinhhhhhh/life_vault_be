package com.moon.vault.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class VaultItemTagId implements Serializable {

    @Column(name = "vault_item_code", length = 100, nullable = false)
    private String vaultItemCode;

    @Column(name = "tag_code", length = 100, nullable = false)
    private String tagCode;
}
