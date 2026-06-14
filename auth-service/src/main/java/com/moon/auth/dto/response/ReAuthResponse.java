package com.moon.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReAuthResponse {

    private String reauthToken;
    private long expiresIn;
}

