package com.moon.vault.controller;

import com.moon.vault.common.ResultResp;
import com.moon.vault.config.OpenApiConfig;
import com.moon.vault.dto.response.ReminderResponse;
import com.moon.vault.security.CurrentUserService;
import com.moon.vault.service.ReminderService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vault/reminders")
@RequiredArgsConstructor
@Tag(name = "Reminders", description = "Reminder list/read/dismiss APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ReminderController {

    private final CurrentUserService currentUserService;
    private final ReminderService reminderService;

    @GetMapping
    public ResultResp<List<ReminderResponse>> list(HttpServletRequest request) {
        return ResultResp.success(reminderService.list(currentUserService.userCode(request)));
    }

    @PostMapping("/{reminderCode}/read")
    public ResultResp<ReminderResponse> read(@PathVariable String reminderCode, HttpServletRequest request) {
        return ResultResp.success(reminderService.markRead(currentUserService.userCode(request), reminderCode, request));
    }

    @PostMapping("/{reminderCode}/dismiss")
    public ResultResp<ReminderResponse> dismiss(@PathVariable String reminderCode, HttpServletRequest request) {
        return ResultResp.success(reminderService.dismiss(currentUserService.userCode(request), reminderCode, request));
    }
}
