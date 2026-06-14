package com.moon.vault.repository;

import com.moon.vault.entity.VaultItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VaultItemRepository extends JpaRepository<VaultItem, Long> {

    Optional<VaultItem> findByVaultItemCodeAndUserCode(String vaultItemCode, String userCode);

    List<VaultItem> findByUserCodeOrderByCreateDateDesc(String userCode);

    List<VaultItem> findByUserCodeAndVaultItemCodeIn(String userCode, Collection<String> vaultItemCodes);

    boolean existsByFolderCodeAndUserCodeAndWorkflowState(String folderCode, String userCode, String workflowState);

    List<VaultItem> findByUserCodeAndExpirationDateBetweenAndWorkflowState(
            String userCode,
            LocalDate fromDate,
            LocalDate toDate,
            String workflowState
    );
}
