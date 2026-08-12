package com.jollifiy.backoffice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgressSaveRequest {
    private String playerId;
    private Integer currentLevel;
    private Integer totalCoins;
    // Oyunda skor da tutulacaksa buraya "private Integer totalScore;" eklenmeli.
}