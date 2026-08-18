package com.jollifiy.backoffice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "player")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false, unique = true)
    private String playerId;

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "country")
    private String country;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Yeni eklenen oyuncu engelleme (ban) durumu alanı
    @Column(name = "is_banned")
    private boolean isBanned = false;

    // Yeni oyuncu eklerken kullanacağımız constructor
    public Player(String playerId, String deviceId, String country) {
        this.playerId = playerId;
        this.deviceId = deviceId;
        this.country = country;
        this.isBanned = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (!isBanned) {
            this.isBanned = false;
        }
    }
}