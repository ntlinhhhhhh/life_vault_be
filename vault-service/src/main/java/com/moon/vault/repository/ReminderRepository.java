package com.moon.vault.repository;

import com.moon.vault.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByUserCodeOrderByRemindAtAsc(String userCode);

    Optional<Reminder> findByReminderCodeAndUserCode(String reminderCode, String userCode);

    boolean existsByVaultItemCodeAndReminderTypeAndStatusIn(String vaultItemCode, String reminderType, Collection<String> statuses);

    List<Reminder> findByStatusAndRemindAtLessThanEqual(String status, LocalDateTime remindAt);
}
