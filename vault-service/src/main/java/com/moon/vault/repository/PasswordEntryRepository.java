package com.moon.vault.repository;

import com.moon.vault.entity.PasswordEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordEntryRepository extends JpaRepository<PasswordEntry, Long> {

    Optional<PasswordEntry> findByVaultItemCode(String vaultItemCode);
}
