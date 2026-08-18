package com.jollifiy.backoffice.service;

import com.jollifiy.backoffice.entity.Analytics;
import com.jollifiy.backoffice.entity.AppConfig;
import com.jollifiy.backoffice.entity.Player;
import com.jollifiy.backoffice.entity.Progress;
import com.jollifiy.backoffice.repository.AnalyticsRepository;
import com.jollifiy.backoffice.repository.AppConfigRepository;
import com.jollifiy.backoffice.repository.PlayerRepository;
import com.jollifiy.backoffice.repository.ProgressRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BackofficeService {

    private final AnalyticsRepository analyticsRepository;
    private final PlayerRepository playerRepository;
    private final ProgressRepository progressRepository;
    private final AppConfigRepository appConfigRepository;

    public BackofficeService(AnalyticsRepository analyticsRepository,
                             PlayerRepository playerRepository,
                             ProgressRepository progressRepository,
                             AppConfigRepository appConfigRepository) {
        this.analyticsRepository = analyticsRepository;
        this.playerRepository = playerRepository;
        this.progressRepository = progressRepository;
        this.appConfigRepository = appConfigRepository;
    }

    // Analytics İşlemleri
    public List<Analytics> getAllAnalytics() { return analyticsRepository.findAll(); }
    public void saveAnalytics(Analytics analytics) { analyticsRepository.save(analytics); }
    public Analytics updateAnalytics(Long id, Analytics analyticsDetails) {
        Analytics analytics = analyticsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Analitik kaydı bulunamadı: " + id));
        analytics.setPlayerId(analyticsDetails.getPlayerId());
        analytics.setEventName(analyticsDetails.getEventName());
        analytics.setLevel(analyticsDetails.getLevel());
        analytics.setScore(analyticsDetails.getScore());
        analytics.setCoinCount(analyticsDetails.getCoinCount());
        analytics.setPlayTime(analyticsDetails.getPlayTime());
        return analyticsRepository.save(analytics);
    }
    public void deleteAnalytics(Long id) { analyticsRepository.deleteById(id); }

    // Player İşlemleri
    public List<Player> getAllPlayers() { return playerRepository.findAll(); }
    public Player updatePlayer(Long id, Player playerDetails) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oyuncu bulunamadı: " + id));
        player.setPlayerId(playerDetails.getPlayerId());
        player.setCountry(playerDetails.getCountry());
        player.setBanned(playerDetails.isBanned());
        return playerRepository.save(player);
    }
    public void deletePlayer(Long id) { playerRepository.deleteById(id); }

    // Progress İşlemleri
    public List<Progress> getAllProgress() { return progressRepository.findAll(); }
    public Progress updateProgress(Long id, Progress progressDetails) {
        Progress progress = progressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İlerleme kaydı bulunamadı: " + id));
        progress.setPlayerId(progressDetails.getPlayerId());
        progress.setCurrentLevel(progressDetails.getCurrentLevel());
        progress.setTotalCoins(progressDetails.getTotalCoins());
        return progressRepository.save(progress);
    }
    public void deleteProgress(Long id) { progressRepository.deleteById(id); }

    // Config İşlemleri
    public List<AppConfig> getAllConfigs() { return appConfigRepository.findAll(); }
    public void saveConfig(AppConfig config) { appConfigRepository.save(config); }

    public AppConfig updateConfig(Long id, AppConfig configDetails) {
        AppConfig config = appConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Konfigürasyon bulunamadı: " + id));

        config.setConfigKey(configDetails.getConfigKey());
        config.setConfigValue(configDetails.getConfigValue());
        config.setIsActive(configDetails.getIsActive()); // <-- AKTİF/PASİF DURUMUNUN GÜNCELLENMESİ İÇİN BU EKLENDİ

        return appConfigRepository.save(config);
    }

    public void deleteConfig(Long id) { appConfigRepository.deleteById(id); }
}