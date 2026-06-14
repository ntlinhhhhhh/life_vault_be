package com.moon.vault.service.impl;

import com.moon.vault.common.AppConstants;
import com.moon.vault.dto.response.ReminderResponse;
import com.moon.vault.entity.Reminder;
import com.moon.vault.exception.VaultException;
import com.moon.vault.repository.ReminderRepository;
import com.moon.vault.service.AuditLogService;
import com.moon.vault.service.ReminderService;
import com.moon.vault.util.CodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderServiceImpl implements ReminderService {

    private static final List<String> OPEN_STATUSES = List.of(AppConstants.STATUS_PENDING, AppConstants.STATUS_SENT);

    private final ReminderRepository reminderRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> list(String userCode) {
        return reminderRepository.findByUserCodeOrderByRemindAtAsc(userCode).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ReminderResponse markRead(String userCode, String reminderCode, HttpServletRequest request) {
        Reminder reminder = getReminder(userCode, reminderCode);
        reminder.setStatus(AppConstants.STATUS_READ);
        reminder.setUpdateUser(userCode);
        auditLogService.record(userCode, "READ_REMINDER", "REMINDER", reminderCode, request);
        return toResponse(reminderRepository.save(reminder));
    }

    @Override
    @Transactional
    public ReminderResponse dismiss(String userCode, String reminderCode, HttpServletRequest request) {
        Reminder reminder = getReminder(userCode, reminderCode);
        reminder.setStatus(AppConstants.STATUS_DISMISSED);
        reminder.setUpdateUser(userCode);
        auditLogService.record(userCode, "DISMISS_REMINDER", "REMINDER", reminderCode, request);
        return toResponse(reminderRepository.save(reminder));
    }

    @Override
    @Transactional
    public void ensureExpirationReminders(String userCode, String itemCode, LocalDate expirationDate) {
        if (expirationDate == null) {
            return;
        }
        ensureOne(userCode, itemCode, expirationDate.minusDays(30).atTime(9, 0), "EXPIRE_30_DAYS");
        ensureOne(userCode, itemCode, expirationDate.minusDays(7).atTime(9, 0), "EXPIRE_7_DAYS");
    }

    @Override
    @Transactional
    public void markDueRemindersSent() {
        reminderRepository.findByStatusAndRemindAtLessThanEqual(AppConstants.STATUS_PENDING, LocalDateTime.now())
                .forEach(reminder -> {
                    reminder.setStatus(AppConstants.STATUS_SENT);
                    reminder.setSentAt(LocalDateTime.now());
                    reminderRepository.save(reminder);
                });
    }

    private void ensureOne(String userCode, String itemCode, LocalDateTime remindAt, String type) {
        if (reminderRepository.existsByVaultItemCodeAndReminderTypeAndStatusIn(itemCode, type, OPEN_STATUSES)) {
            return;
        }
        Reminder reminder = new Reminder();
        reminder.setReminderCode(CodeGenerator.generate(AppConstants.REMINDER_CODE_PREFIX));
        reminder.setVaultItemCode(itemCode);
        reminder.setUserCode(userCode);
        reminder.setReminderType(type);
        reminder.setRemindAt(remindAt.isBefore(LocalDateTime.now()) ? LocalDateTime.now() : remindAt);
        reminder.setStatus(AppConstants.STATUS_PENDING);
        reminder.setCreateUser(userCode);
        reminderRepository.save(reminder);
    }

    private Reminder getReminder(String userCode, String reminderCode) {
        return reminderRepository.findByReminderCodeAndUserCode(reminderCode, userCode)
                .orElseThrow(() -> VaultException.notFound("Reminder not found"));
    }

    private ReminderResponse toResponse(Reminder reminder) {
        return new ReminderResponse(
                reminder.getReminderCode(),
                reminder.getVaultItemCode(),
                reminder.getReminderType(),
                reminder.getStatus(),
                reminder.getRemindAt(),
                reminder.getSentAt()
        );
    }
}
