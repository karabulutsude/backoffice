package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Analytics;
import com.jollifiy.backoffice.service.BackofficeService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Dashboard | Jollify Game Analytics")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<Analytics> grid = new Grid<>(Analytics.class);
    private final TextField searchField = new TextField();
    private final Select<String> dateFilterSelect = new Select<>();
    private VerticalLayout eventCardsLayoutContainer;

    private String currentFilterType = "ALL";

    @Autowired
    public DashboardView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Başlık, Canlı Rozet, Tarih Filtresi, Yenile ve Export Alanı
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 mainTitle = new H1("Oyun Analitiği Genel Bakış");
        mainTitle.getStyle().set("margin", "0");

        HorizontalLayout headerButtons = new HorizontalLayout();
        headerButtons.setAlignItems(Alignment.CENTER);

        HorizontalLayout liveBadge = createLiveBadge();

        dateFilterSelect.setItems("Tüm Zamanlar", "Bugün", "Son 7 Gün", "Bu Ay");
        dateFilterSelect.setValue("Tüm Zamanlar");
        dateFilterSelect.setWidth("160px");
        dateFilterSelect.addValueChangeListener(e -> updateTableList());

        Anchor downloadAnchor = createCsvDownloadAnchor();
        Button refreshBtn = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> refreshDashboardData());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        headerButtons.add(liveBadge, dateFilterSelect, downloadAnchor, refreshBtn);
        headerLayout.add(mainTitle, headerButtons);
        add(headerLayout);

        // 1. İstatistik Kartları (Üst Satır - 4 Eşit Kart)
        HorizontalLayout topCardsLayout = new HorizontalLayout();
        topCardsLayout.setWidthFull();
        topCardsLayout.setSpacing(true);

        long playerCount = 0;
        long analyticsCount = 0;
        long progressCount = 0;
        try {
            playerCount = backofficeService.getAllPlayers().size();
            analyticsCount = backofficeService.getAllAnalytics().size();
            progressCount = backofficeService.getAllProgress().size();
        } catch (Exception ignored) {}

        VerticalLayout playerCard = createStatCard("Toplam Oyuncu", String.valueOf(playerCount), VaadinIcon.USERS.create(), "#3b82f6");
        VerticalLayout analyticsCard = createStatCard("Toplam Analitik Olayı", String.valueOf(analyticsCount), VaadinIcon.CHART.create(), "#10b981");
        VerticalLayout progressCard = createStatCard("İlerleme Kayıtları", String.valueOf(progressCount), VaadinIcon.FLAG.create(), "#8b5cf6");

        String maxScoreText = getMaxScoreDetails();
        VerticalLayout topScoreCard = createStatCard("En Yüksek Skor", maxScoreText, VaadinIcon.TROPHY.create(), "#f59e0b");

        topCardsLayout.add(playerCard, analyticsCard, progressCard, topScoreCard);
        add(topCardsLayout);

        // 2. Liderlik Tablosu (Tam Genişlik)
        VerticalLayout leaderboardCard = createLeaderboardCard();
        add(leaderboardCard);

        // 3. 3'lü Başarı Oranı Kartları (GameScenes, Level 2, Level 3)
        HorizontalLayout winRateCardsLayout = createLevelWinRateCards();
        add(winRateCardsLayout);

        // 4. Olay Türü Özet Kartları
        H3 summaryTitle = new H3("Olay Türü Özet Kartları");
        summaryTitle.getStyle().set("margin-top", "10px");

        eventCardsLayoutContainer = new VerticalLayout();
        eventCardsLayoutContainer.setPadding(false);
        eventCardsLayoutContainer.setSpacing(false);
        updateEventSummaryCards();

        add(summaryTitle, eventCardsLayoutContainer);

        // 5. Son Aktiviteler Tablosu, Hızlı Filtreler ve Arama Alanı
        HorizontalLayout tableHeaderLayout = new HorizontalLayout();
        tableHeaderLayout.setWidthFull();
        tableHeaderLayout.setAlignItems(Alignment.CENTER);
        tableHeaderLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        HorizontalLayout leftTitleAndFilters = new HorizontalLayout();
        leftTitleAndFilters.setAlignItems(Alignment.CENTER);
        leftTitleAndFilters.setSpacing(true);

        H3 tableTitle = new H3("Son Gerçekleşen Olaylar");
        tableTitle.getStyle().set("margin", "0");

        Button filterAllBtn = new Button("Tümü", e -> applyQuickFilter("ALL"));
        filterAllBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        Button filterWinBtn = new Button("Kazananlar (WIN)", e -> applyQuickFilter("WIN"));
        filterWinBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        Button filterLoseBtn = new Button("Kaybedenler (LOSE)", e -> applyQuickFilter("LOSE"));
        filterLoseBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);

        HorizontalLayout filterButtonsLayout = new HorizontalLayout(filterAllBtn, filterWinBtn, filterLoseBtn);
        filterButtonsLayout.setSpacing(true);

        leftTitleAndFilters.add(tableTitle, filterButtonsLayout);

        searchField.setPlaceholder("Olay adı veya Oyuncu ID ara...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateTableList());
        searchField.setWidth("300px");

        tableHeaderLayout.add(leftTitleAndFilters, searchField);

        grid.setColumns("playerId", "eventName", "level", "score", "createdAt");
        grid.getColumnByKey("playerId").setHeader("Oyuncu ID");
        grid.getColumnByKey("eventName").setHeader("Olay Adı");
        grid.getColumnByKey("level").setHeader("Seviye");
        grid.getColumnByKey("score").setHeader("Skor");

        grid.getColumnByKey("createdAt").setHeader("Zaman").setRenderer(new com.vaadin.flow.data.renderer.TextRenderer<>(analytics -> {
            if (analytics.getCreatedAt() == null) return "-";
            try {
                return analytics.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            } catch (Exception e) {
                return analytics.getCreatedAt().toString();
            }
        }));

        // Satıra tıklandığında oyuncunun detay modalını aç
        grid.addItemClickListener((com.vaadin.flow.component.grid.ItemClickEvent<Analytics> event) -> {
            Analytics selectedEvent = event.getItem();
            if (selectedEvent != null && selectedEvent.getPlayerId() != null) {
                showPlayerDetailDialog(selectedEvent.getPlayerId());
            }
        });

        grid.setHeight("220px");
        updateTableList();

        add(tableHeaderLayout, grid);
    }

    private void showPlayerDetailDialog(String playerId) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Oyuncu Detayları: " + playerId);
        dialog.setWidth("700px");
        dialog.setHeight("500px");

        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setSizeFull();
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        List<Analytics> playerEvents = List.of();
        try {
            playerEvents = backofficeService.getAllAnalytics().stream()
                    .filter(ev -> playerId.equals(ev.getPlayerId()))
                    .collect(Collectors.toList());
        } catch (Exception ignored) {}

        Span infoSpan = new Span("Toplam Olay Sayısı: " + playerEvents.size());
        infoSpan.getStyle().set("font-weight", "600");
        infoSpan.getStyle().set("color", "#64748b");

        Grid<Analytics> playerGrid = new Grid<>(Analytics.class);
        playerGrid.setItems(playerEvents);
        playerGrid.setColumns("eventName", "level", "score", "createdAt");
        playerGrid.getColumnByKey("eventName").setHeader("Olay Adı");
        playerGrid.getColumnByKey("level").setHeader("Seviye");
        playerGrid.getColumnByKey("score").setHeader("Skor");
        playerGrid.getColumnByKey("createdAt").setHeader("Zaman").setRenderer(new com.vaadin.flow.data.renderer.TextRenderer<>(analytics -> {
            if (analytics.getCreatedAt() == null) return "-";
            try {
                return analytics.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
            } catch (Exception e) {
                return analytics.getCreatedAt().toString();
            }
        }));
        playerGrid.setSizeFull();

        dialogLayout.add(infoSpan, playerGrid);
        dialog.add(dialogLayout);

        Button closeBtn = new Button("Kapat", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(closeBtn);

        dialog.open();
    }

    private void applyQuickFilter(String filterType) {
        this.currentFilterType = filterType;
        updateTableList();
    }

    private HorizontalLayout createLiveBadge() {
        HorizontalLayout badgeLayout = new HorizontalLayout();
        badgeLayout.setAlignItems(Alignment.CENTER);
        badgeLayout.setSpacing(false);
        badgeLayout.getStyle().set("background-color", "#ecfdf5");
        badgeLayout.getStyle().set("border", "1px solid #10b98133");
        badgeLayout.getStyle().set("padding", "4px 10px");
        badgeLayout.getStyle().set("border-radius", "20px");
        badgeLayout.getStyle().set("margin-right", "10px");

        Span dot = new Span();
        dot.getStyle().set("width", "8px");
        dot.getStyle().set("height", "8px");
        dot.getStyle().set("background-color", "#10b981");
        dot.getStyle().set("border-radius", "50%");
        dot.getStyle().set("display", "inline-block");
        dot.getStyle().set("margin-right", "6px");
        dot.getStyle().set("box-shadow", "0 0 0 0 rgba(16, 185, 129, 0.7)");

        Span text = new Span("CANLI");
        text.getStyle().set("font-size", "11px");
        text.getStyle().set("font-weight", "bold");
        text.getStyle().set("color", "#047857");

        badgeLayout.add(dot, text);
        return badgeLayout;
    }

    private VerticalLayout createLeaderboardCard() {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setWidthFull();
        card.getStyle().set("background-color", "#ffffff");
        card.getStyle().set("border-radius", "10px");
        card.getStyle().set("border", "1px solid #e2e8f0");
        card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setAlignItems(Alignment.CENTER);

        Icon icon = VaadinIcon.MEDAL.create();
        icon.getStyle().set("color", "#ec4899");
        icon.getStyle().set("background-color", "#ec489915");
        icon.getStyle().set("padding", "8px");
        icon.getStyle().set("border-radius", "8px");

        H3 titleH3 = new H3("En İyi Oyuncular Liderlik Tablosu");
        titleH3.getStyle().set("margin", "0 0 0 10px");
        titleH3.getStyle().set("font-size", "15px");
        titleH3.getStyle().set("color", "#64748b");

        topLayout.add(icon, titleH3);
        card.add(topLayout);

        try {
            List<Analytics> events = backofficeService.getAllAnalytics();
            if (events.isEmpty()) {
                card.add(new Span("Henüz skor kaydı yok."));
                return card;
            }

            Map<String, Integer> topPlayersMap = events.stream()
                    .collect(Collectors.toMap(
                            Analytics::getPlayerId,
                            Analytics::getScore,
                            Integer::max
                    ));

            List<Map.Entry<String, Integer>> sortedPlayers = topPlayersMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .limit(3)
                    .collect(Collectors.toList());

            int rank = 1;
            for (Map.Entry<String, Integer> entry : sortedPlayers) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.setJustifyContentMode(JustifyContentMode.BETWEEN);
                row.setAlignItems(Alignment.CENTER);
                row.getStyle().set("padding", "6px 0");
                row.getStyle().set("border-bottom", "1px solid #f1f5f9");

                Span playerSpan = new Span(rank + ". Oyuncu ID: " + entry.getKey());
                playerSpan.getStyle().set("font-size", "13px");
                playerSpan.getStyle().set("color", "#334155");

                Span scoreSpan = new Span(entry.getValue() + " Puan");
                scoreSpan.getStyle().set("font-size", "13px");
                scoreSpan.getStyle().set("font-weight", "bold");
                scoreSpan.getStyle().set("color", "#0f172a");

                row.add(playerSpan, scoreSpan);
                card.add(row);
                rank++;
            }

        } catch (Exception e) {
            card.add(new Span("Veriler yüklenemedi."));
        }

        return card;
    }

    private HorizontalLayout createLevelWinRateCards() {
        HorizontalLayout container = new HorizontalLayout();
        container.setWidthFull();
        container.setSpacing(true);

        container.add(createSingleWinRateCard("GameScenes Başarı Oranı", "GAMESCENES", "#3b82f6", VaadinIcon.GAMEPAD.create()));
        container.add(createSingleWinRateCard("Level 2 Başarı Oranı", "LEVEL2", "#10b981", VaadinIcon.CHART.create()));
        container.add(createSingleWinRateCard("Level 3 Başarı Oranı", "LEVEL3", "#8b5cf6", VaadinIcon.TROPHY.create()));

        return container;
    }

    private VerticalLayout createSingleWinRateCard(String cardTitle, String keyword, String colorHex, Icon icon) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.getStyle().set("background-color", "#ffffff");
        card.getStyle().set("border-radius", "10px");
        card.getStyle().set("border", "1px solid #e2e8f0");
        card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");
        card.setWidthFull();

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setAlignItems(Alignment.CENTER);

        icon.getStyle().set("color", colorHex);
        icon.getStyle().set("background-color", colorHex + "15");
        icon.getStyle().set("padding", "8px");
        icon.getStyle().set("border-radius", "8px");

        H3 titleH3 = new H3(cardTitle);
        titleH3.getStyle().set("margin", "0 0 0 10px");
        titleH3.getStyle().set("font-size", "14px");
        titleH3.getStyle().set("color", "#64748b");

        topLayout.add(icon, titleH3);

        try {
            List<Analytics> events = backofficeService.getAllAnalytics();

            List<Analytics> keywordEvents = events.stream()
                    .filter(ev -> ev.getEventName() != null && ev.getEventName().toUpperCase().contains(keyword))
                    .collect(Collectors.toList());

            long totalCount = keywordEvents.size();

            long winCount = keywordEvents.stream()
                    .filter(ev -> ev.getEventName().toUpperCase().contains("WIN"))
                    .count();

            double winRate = totalCount > 0 ? (double) winCount / totalCount : 0.0;

            ProgressBar progressBar = new ProgressBar(0.0, 1.0, winRate);
            progressBar.setWidthFull();
            progressBar.getStyle().set("--lumo-primary-color", colorHex);

            Span rateText = new Span(String.format("%%%.1f (%d/%d)", winRate * 100, winCount, totalCount));
            rateText.getStyle().set("font-size", "20px");
            rateText.getStyle().set("font-weight", "bold");
            rateText.getStyle().set("color", "#0f172a");

            card.add(topLayout, rateText, progressBar);

        } catch (Exception e) {
            card.add(topLayout, new Span("Hesaplanamadı"));
        }

        return card;
    }

    private String getMaxScoreDetails() {
        try {
            List<Analytics> events = backofficeService.getAllAnalytics();
            if (events.isEmpty()) return "0";

            Analytics maxEvent = events.stream()
                    .max(Comparator.comparingInt(Analytics::getScore))
                    .orElse(null);

            if (maxEvent != null) {
                return maxEvent.getScore() + " Puan";
            }
        } catch (Exception ignored) {}
        return "0";
    }

    private Anchor createCsvDownloadAnchor() {
        Button exportBtn = new Button("Raporu İndir (CSV)", VaadinIcon.DOWNLOAD.create());
        exportBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        StreamResource streamResource = new StreamResource("jollify_analytics_report.csv", () -> {
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Player ID,Event Name,Level,Score,Created At\n");

            try {
                List<Analytics> events = backofficeService.getAllAnalytics();
                for (Analytics ev : events) {
                    csvContent.append(ev.getPlayerId()).append(",")
                            .append(ev.getEventName()).append(",")
                            .append(ev.getLevel()).append(",")
                            .append(ev.getScore()).append(",")
                            .append(ev.getCreatedAt()).append("\n");
                }
            } catch (Exception e) {
                csvContent.append("Veriler alınırken hata oluştu.");
            }

            getUI().ifPresent(ui -> ui.access(() -> {
                Notification notif = Notification.show("CSV raporu başarıyla indirildi!", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }));

            return new ByteArrayInputStream(csvContent.toString().getBytes(StandardCharsets.UTF_8));
        });

        Anchor anchor = new Anchor(streamResource, "");
        anchor.add(exportBtn);
        anchor.setTarget("_blank");
        return anchor;
    }

    private void refreshDashboardData() {
        UI.getCurrent().getPage().reload();
    }

    private void updateTableList() {
        try {
            List<Analytics> allEvents = backofficeService.getAllAnalytics();

            List<Analytics> filteredList = allEvents.stream().filter(event -> {
                String eventName = event.getEventName() != null ? event.getEventName().toUpperCase() : "";
                if ("WIN".equals(currentFilterType)) {
                    return eventName.contains("WIN");
                } else if ("LOSE".equals(currentFilterType)) {
                    return eventName.contains("LOSE");
                }
                return true;
            }).collect(Collectors.toList());

            String selectedDateFilter = dateFilterSelect.getValue();
            LocalDateTime now = LocalDateTime.now();
            if (selectedDateFilter != null) {
                filteredList = filteredList.stream().filter(event -> {
                    if (event.getCreatedAt() == null) return false;
                    switch (selectedDateFilter) {
                        case "Bugün":
                            return event.getCreatedAt().toLocalDate().isEqual(now.toLocalDate());
                        case "Son 7 Gün":
                            return event.getCreatedAt().isAfter(now.minusDays(7));
                        case "Bu Ay":
                            return event.getCreatedAt().getYear() == now.getYear() &&
                                    event.getCreatedAt().getMonth() == now.getMonth();
                        default:
                            return true;
                    }
                }).collect(Collectors.toList());
            }

            String filterText = searchField.getValue();
            if (filterText != null && !filterText.isEmpty()) {
                filteredList = filteredList.stream().filter(event ->
                        (event.getEventName() != null && event.getEventName().toLowerCase().contains(filterText.toLowerCase())) ||
                                (event.getPlayerId() != null && event.getPlayerId().toLowerCase().contains(filterText.toLowerCase()))
                ).collect(Collectors.toList());
            }

            grid.setItems(filteredList);
        } catch (Exception ignored) {}
    }

    private void updateEventSummaryCards() {
        eventCardsLayoutContainer.removeAll();
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setWidthFull();

        try {
            List<Analytics> events = backofficeService.getAllAnalytics();
            if (events.isEmpty()) {
                layout.add(new Span("Gösterilecek olay verisi bulunamadı."));
                eventCardsLayoutContainer.add(layout);
                return;
            }

            Map<String, Long> eventCounts = events.stream()
                    .collect(Collectors.groupingBy(Analytics::getEventName, Collectors.counting()));

            eventCounts.forEach((eventName, count) -> {
                VerticalLayout miniCard = new VerticalLayout();
                miniCard.setPadding(true);
                miniCard.getStyle().set("background-color", "#ffffff");
                miniCard.getStyle().set("border", "1px solid #e2e8f0");
                miniCard.getStyle().set("border-radius", "10px");
                miniCard.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");
                miniCard.setWidth("200px");

                Span nameSpan = new Span(eventName);
                nameSpan.getStyle().set("font-size", "13px");
                nameSpan.getStyle().set("font-weight", "600");
                nameSpan.getStyle().set("color", "#64748b");

                Span countSpan = new Span(count + " Adet");
                countSpan.getStyle().set("font-size", "20px");
                countSpan.getStyle().set("font-weight", "bold");
                countSpan.getStyle().set("color", "#0f172a");

                miniCard.add(nameSpan, countSpan);
                layout.add(miniCard);
            });

            eventCardsLayoutContainer.add(layout);

        } catch (Exception e) {
            eventCardsLayoutContainer.add(new Span("Veriler yüklenemedi."));
        }
    }

    private VerticalLayout createStatCard(String title, String value, Icon icon, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.getStyle().set("background-color", "#ffffff");
        card.getStyle().set("border-radius", "10px");
        card.getStyle().set("border", "1px solid #e2e8f0");
        card.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.02)");

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setAlignItems(Alignment.CENTER);

        icon.getStyle().set("color", color);
        icon.getStyle().set("background-color", color + "15");
        icon.getStyle().set("padding", "8px");
        icon.getStyle().set("border-radius", "8px");

        H3 titleH3 = new H3(title);
        titleH3.getStyle().set("margin", "0 0 0 10px");
        titleH3.getStyle().set("font-size", "14px");
        titleH3.getStyle().set("color", "#64748b");

        topLayout.add(icon, titleH3);

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("font-size", "22px");
        valueSpan.getStyle().set("font-weight", "bold");
        valueSpan.getStyle().set("color", "#0f172a");
        valueSpan.getStyle().set("margin-top", "5px");

        card.add(topLayout, valueSpan);
        return card;
    }
}