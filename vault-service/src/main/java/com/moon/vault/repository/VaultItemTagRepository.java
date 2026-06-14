package com.moon.vault.repository;

import com.moon.vault.entity.VaultItemTag;
import com.moon.vault.entity.VaultItemTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface VaultItemTagRepository extends JpaRepository<VaultItemTag, VaultItemTagId> {

    List<VaultItemTag> findByIdVaultItemCode(String vaultItemCode);

    List<VaultItemTag> findByIdVaultItemCodeIn(Collection<String> vaultItemCodes);

    List<VaultItemTag> findByIdTagCode(String tagCode);

    void deleteByIdVaultItemCode(String vaultItemCode);

    void deleteByIdTagCode(String tagCode);
}
