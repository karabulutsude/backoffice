package com.jollifiy.backoffice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequest {

    private String playerId;
    private String deviceId;
    private String country;
}