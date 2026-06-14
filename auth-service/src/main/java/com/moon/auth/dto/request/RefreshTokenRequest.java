package com.moon.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Refresh access token request")
public class RefreshTokenRequest {

    @NotBlank
    @Schema(example = "paste-refresh-token-here")
    private String refreshToken;
}

