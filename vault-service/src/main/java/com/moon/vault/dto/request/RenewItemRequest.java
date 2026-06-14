package com.moon.vault.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RenewItemRequest(@NotNull LocalDate newExpirationDate) {
}
