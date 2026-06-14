package com.moon.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Re-authenticate before sensitive actions")
public class ReAuthRequest {

    @NotBlank
    @Schema(example = "Password123")
    private String password;
}

