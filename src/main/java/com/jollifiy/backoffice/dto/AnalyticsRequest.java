package com.jollifiy.backoffice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyticsRequest {
    private String playerId;
    private String eventName;
    private Integer level;
    private Integer score;
    private Integer coinCount;
    private Integer completionPercentage;
    private Double playTime;
    private Integer healthRemaining;
    private String deathReason;
}