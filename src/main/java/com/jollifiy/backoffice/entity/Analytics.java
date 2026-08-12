package com.jollifiy.backoffice.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "analytics")
public class Analytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private String playerId;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "level")
    private Integer level;

    @Column(name = "score")
    private Integer score;

    @Column(name = "coin_count")
    private Integer coinCount;

    @Column(name = "completion_percentage")
    private Integer completionPercentage;

    @Column(name = "play_time")
    private Double playTime;

    @Column(name = "health_remaining")
    private Integer healthRemaining;

    @Column(name = "death_reason")
    private String deathReason;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getter ve Setter Metotları ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getCoinCount() { return coinCount; }
    public void setCoinCount(Integer coinCount) { this.coinCount = coinCount; }

    public Integer getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Integer completionPercentage) { this.completionPercentage = completionPercentage; }

    public Double getPlayTime() { return playTime; }
    public void setPlayTime(Double playTime) { this.playTime = playTime; }

    public Integer getHealthRemaining() { return healthRemaining; }
    public void setHealthRemaining(Integer healthRemaining) { this.healthRemaining = healthRemaining; }

    public String getDeathReason() { return deathReason; }
    public void setDeathReason(String deathReason) { this.deathReason = deathReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}