package com.moon.vault.service;

import com.moon.vault.dto.response.ReminderResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface ReminderService {

    List<ReminderResponse> list(String userCode);

    ReminderResponse markRead(String userCode, String reminderCode, HttpServletRequest request);

    ReminderResponse dismiss(String userCode, String reminderCode, HttpServletRequest request);

    void ensureExpirationReminders(String userCode, String itemCode, java.time.LocalDate expirationDate);

    void markDueRemindersSent();
}
