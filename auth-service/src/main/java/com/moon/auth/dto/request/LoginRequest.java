package com.moon.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Login request")
public class LoginRequest {

    @NotBlank
    @Size(max = 255)
    @Schema(description = "Username or email", example = "testuser")
    private String usernameOrEmail;

    @NotBlank
    @Size(max = 100)
    @Schema(example = "Password123")
    private String password;

    @NotBlank
    @Size(max = 255)
    @Schema(example = "local-dev")
    private String deviceId;

    @Size(max = 255)
    @Schema(example = "Swagger UI")
    private String deviceName;
}

