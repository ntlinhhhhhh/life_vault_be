package com.moon.vault.scheduler;

import com.moon.vault.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final ReminderService reminderService;

    @Scheduled(fixedDelayString = "${vault.reminder.scheduler-delay-ms:60000}")
    public void markDueRemindersSent() {
        reminderService.markDueRemindersSent();
    }
}
