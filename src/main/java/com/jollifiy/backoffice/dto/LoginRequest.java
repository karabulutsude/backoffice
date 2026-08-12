package com.jollifiy.backoffice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {
    private String deviceId;
    private String country;
    private String clientVersion;
}