package com.jollifiy.backoffice.service;

import com.jollifiy.backoffice.entity.Analytics;
import com.jollifiy.backoffice.entity.AppConfig;
import com.jollifiy.backoffice.entity.Progress;
import com.jollifiy.backoffice.entity.Player;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class GameApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://localhost:8090";

    private HttpEntity<String> createAuthHeader() {
        HttpHeaders headers = new HttpHeaders();
        String auth = "admin:jollify123";
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + new String(encodedAuth));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    private <T> HttpEntity<T> createAuthHeader(T body) {
        HttpHeaders headers = new HttpHeaders();
        String auth = "admin:jollify123";
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + new String(encodedAuth));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    public List<Analytics> getAllAnalytics() {
        try {
            var response = restTemplate.exchange(BASE_URL + "/analytics", HttpMethod.GET, createAuthHeader(), Analytics[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("Analitikler çekilirken hata: " + e.getMessage());
            return List.of();
        }
    }

    public void saveAnalytics(Analytics analytics) {
        try {
            restTemplate.exchange(BASE_URL + "/analytics/send", HttpMethod.POST, createAuthHeader(analytics), Analytics.class);
        } catch (Exception e) {
            System.err.println("Analitik kaydedilirken hata: " + e.getMessage());
        }
    }

    public List<AppConfig> getAllConfigs() {
        try {
            var response = restTemplate.exchange(BASE_URL + "/config", HttpMethod.GET, createAuthHeader(), AppConfig[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("Konfigürasyonlar çekilirken hata: " + e.getMessage());
            return List.of();
        }
    }

    public void saveConfig(AppConfig config) {
        try {
            restTemplate.exchange(BASE_URL + "/config/save", HttpMethod.POST, createAuthHeader(config), AppConfig.class);
        } catch (Exception e) {
            System.err.println("Konfigürasyon kaydedilirken hata: " + e.getMessage());
        }
    }

    public List<Player> getAllPlayers() {
        try {
            var response = restTemplate.exchange(BASE_URL + "/players", HttpMethod.GET, createAuthHeader(), Player[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("Oyuncular çekilirken hata: " + e.getMessage());
            return List.of();
        }
    }

    public List<Progress> getAllProgress() {
        try {
            var response = restTemplate.exchange(BASE_URL + "/progress", HttpMethod.GET, createAuthHeader(), Progress[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (Exception e) {
            System.err.println("İlerlemeler çekilirken hata: " + e.getMessage());
            return List.of();
        }
    }
}