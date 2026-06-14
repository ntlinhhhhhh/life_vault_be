package com.moon.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Update profile request")
public class UpdateProfileRequest {

    @Size(max = 100)
    @Schema(example = "testuser_updated")
    private String username;

    @Email
    @Size(max = 255)
    @Schema(example = "test.updated@example.com")
    private String email;
}
