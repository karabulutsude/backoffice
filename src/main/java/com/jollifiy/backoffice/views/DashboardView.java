package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Analytics;
import com.jollifiy.backoffice.service.GameApiClient;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Dashboard | Jollify Game Analytics")
@Route(value = "", layout = MainLayout.class)
@PermitAll
public class DashboardView extends VerticalLayout {

    private final GameApiClient gameApiClient;
    private final Grid<Analytics> grid = new Grid<>(Analytics.class);
    private final TextField searchField = new TextField();
    private VerticalLayout eventCardsLayoutContainer;

    @Autowired
    public DashboardView(GameApiClient gameApiClient) {
        this.gameApiClient = gameApiClient;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Başlık, Yenile ve Export Alanı
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidthFull();
        headerLayout.setAlignItems(Alignment.CENTER);
        headerLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H1 mainTitle = new H1("Oyun Analitiği Genel Bakış");
        mainTitle.getStyle().set("margin", "0");

        HorizontalLayout headerButtons = new HorizontalLayout();
        Anchor downloadAnchor = createCsvDownloadAnchor();
        Button refreshBtn = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> refreshDashboardData());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        headerButtons.add(downloadAnchor, refreshBtn);
        headerLayout.add(mainTitle, headerButtons);
        add(headerLayout);

        // 1. İstatistik Kartları (4'lü Modern Yapı)
        HorizontalLayout cardsLayout = new HorizontalLayout();
        cardsLayout.setWidthFull();
        cardsLayout.setSpacing(true);

        long playerCount = 0;
        long analyticsCount = 0;
        long progressCount = 0;
        try {
            playerCount = gameApiClient.getAllPlayers().size();
            analyticsCount = gameApiClient.getAllAnalytics().size();
            progressCount = gameApiClient.getAllProgress().size();
        } catch (Exception ignored) {}

        VerticalLayout playerCard = createStatCard("Toplam Oyuncu", String.valueOf(playerCount), VaadinIcon.USERS.create(), "#3b82f6");
        VerticalLayout analyticsCard = createStatCard("Toplam Analitik Olayı", String.valueOf(analyticsCount), VaadinIcon.CHART.create(), "#10b981");
        VerticalLayout progressCard = createStatCard("İlerleme Kayıtları", String.valueOf(progressCount), VaadinIcon.FLAG.create(), "#8b5cf6");

        String maxScoreText = getMaxScoreDetails();
        VerticalLayout topScoreCard = createStatCard("En Yüksek Skor", maxScoreText, VaadinIcon.TROPHY.create(), "#f59e0b");

        cardsLayout.add(playerCard, analyticsCard, progressCard, topScoreCard);
        add(cardsLayout);

        // 2. Hızlı Aksiyon Butonları
        H3 actionTitle = new H3("Hızlı İşlemler");
        actionTitle.getStyle().set("margin-top", "15px");

        Button btnAnalytics = new Button("Analitiklere Git", VaadinIcon.CHART.create(), e -> UI.getCurrent().navigate(AnalyticsView.class));
        btnAnalytics.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button btnPlayers = new Button("Oyuncuları Görüntüle", VaadinIcon.USERS.create(), e -> UI.getCurrent().navigate(PlayerView.class));
        Button btnConfig = new Button("Konfigürasyon Ekle", VaadinIcon.COG.create(), e -> UI.getCurrent().navigate(ConfigView.class));

        HorizontalLayout actionsLayout = new HorizontalLayout(btnAnalytics, btnPlayers, btnConfig);
        actionsLayout.setSpacing(true);

        add(actionTitle, actionsLayout);

        // 3. Olay Türleri İçin Modern Grid Kartları Alanı
        H3 summaryTitle = new H3("Olay Türü Özet Kartları");
        summaryTitle.getStyle().set("margin-top", "15px");

        eventCardsLayoutContainer = new VerticalLayout();
        eventCardsLayoutContainer.setPadding(false);
        eventCardsLayoutContainer.setSpacing(false);
        updateEventSummaryCards();

        add(summaryTitle, eventCardsLayoutContainer);

        // 4. Son Aktiviteler Tablosu ve Arama Alanı
        HorizontalLayout tableHeaderLayout = new HorizontalLayout();
        tableHeaderLayout.setWidthFull();
        tableHeaderLayout.setAlignItems(Alignment.CENTER);
        tableHeaderLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);

        H3 tableTitle = new H3("Son Gerçekleşen Olaylar");
        tableTitle.getStyle().set("margin", "0");

        searchField.setPlaceholder("Olay adı veya Oyuncu ID ara...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateTableList());
        searchField.setWidth("300px");

        tableHeaderLayout.add(tableTitle, searchField);

        grid.setColumns("playerId", "eventName", "level", "score", "createdAt");
        grid.getColumnByKey("playerId").setHeader("Oyuncu ID");
        grid.getColumnByKey("eventName").setHeader("Olay Adı");
        grid.getColumnByKey("level").setHeader("Seviye");
        grid.getColumnByKey("score").setHeader("Skor");
        grid.getColumnByKey("createdAt").setHeader("Zaman");

        grid.setHeight("220px");
        updateTableList();

        add(tableHeaderLayout, grid);
    }

    private String getMaxScoreDetails() {
        try {
            List<Analytics> events = gameApiClient.getAllAnalytics();
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
                List<Analytics> events = gameApiClient.getAllAnalytics();
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
            List<Analytics> allEvents = gameApiClient.getAllAnalytics();
            String filterText = searchField.getValue();

            if (filterText == null || filterText.isEmpty()) {
                grid.setItems(allEvents);
            } else {
                List<Analytics> filtered = allEvents.stream().filter(event ->
                        (event.getEventName() != null && event.getEventName().toLowerCase().contains(filterText.toLowerCase())) ||
                                (event.getPlayerId() != null && event.getPlayerId().toLowerCase().contains(filterText.toLowerCase()))
                ).collect(Collectors.toList());
                grid.setItems(filtered);
            }
        } catch (Exception ignored) {}
    }

    private void updateEventSummaryCards() {
        eventCardsLayoutContainer.removeAll();
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setWidthFull();

        try {
            List<Analytics> events = gameApiClient.getAllAnalytics();
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
        valueSpan.getStyle().set("font-size", "26px");
        valueSpan.getStyle().set("font-weight", "bold");
        valueSpan.getStyle().set("color", "#0f172a");
        valueSpan.getStyle().set("margin-top", "5px");

        card.add(topLayout, valueSpan);
        return card;
    }
}