package com.moon.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Registration request")
public class RegisterRequest {

    @NotBlank
    @Size(max = 100)
    @Schema(example = "testuser")
    private String username;

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(example = "test@example.com")
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(example = "Password123")
    private String password;
}

