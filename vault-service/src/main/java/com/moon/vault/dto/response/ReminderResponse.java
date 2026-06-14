package com.moon.vault.dto.response;

import java.time.LocalDateTime;

public record ReminderResponse(
        String reminderCode,
        String vaultItemCode,
        String reminderType,
        String status,
        LocalDateTime remindAt,
        LocalDateTime sentAt
) {
}
