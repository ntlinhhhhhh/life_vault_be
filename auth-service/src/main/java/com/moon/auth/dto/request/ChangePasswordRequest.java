package com.moon.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Change password request")
public class ChangePasswordRequest {

    @NotBlank
    @Schema(example = "Password123")
    private String oldPassword;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(example = "NewPassword123")
    private String newPassword;
}

