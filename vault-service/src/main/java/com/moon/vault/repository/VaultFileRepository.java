package com.moon.vault.repository;

import com.moon.vault.entity.VaultFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VaultFileRepository extends JpaRepository<VaultFile, Long> {

    List<VaultFile> findByVaultItemCodeAndUserCodeAndStatusOrderByCreateDateDesc(String vaultItemCode, String userCode, String status);

    Optional<VaultFile> findByFileCodeAndUserCode(String fileCode, String userCode);

    List<VaultFile> findByVaultItemCodeAndUserCode(String vaultItemCode, String userCode);
}
