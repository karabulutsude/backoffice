package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Analytics;
import com.jollifiy.backoffice.service.GameApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Oyun Analitikleri | Jollify Game Analytics")
@Route(value = "analytics", layout = MainLayout.class)
@PermitAll
public class AnalyticsView extends VerticalLayout {

    private final GameApiClient gameApiClient;
    private final Grid<Analytics> grid = new Grid<>(Analytics.class);

    @Autowired
    public AnalyticsView(GameApiClient gameApiClient) {
        this.gameApiClient = gameApiClient;

        setSizeFull();
        setPadding(true);

        add(new H2("Oyun Analitikleri ve Olaylar"));

        Button addButton = new Button("Yeni Analitik Ekle", VaadinIcon.PLUS.create(), e -> openAddDialog());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> loadData());

        HorizontalLayout toolbar = new HorizontalLayout(addButton, refreshButton);
        toolbar.setWidthFull();

        grid.setColumns("id", "playerId", "eventName", "level", "score", "coinCount", "playTime", "createdAt");

        grid.getColumnByKey("id").setHeader("ID");
        grid.getColumnByKey("playerId").setHeader("Oyuncu ID");
        grid.getColumnByKey("eventName").setHeader("Olay Adı");
        grid.getColumnByKey("level").setHeader("Seviye");
        grid.getColumnByKey("score").setHeader("Skor");
        grid.getColumnByKey("coinCount").setHeader("Altın");
        grid.getColumnByKey("playTime").setHeader("Oynama Süresi");
        grid.getColumnByKey("createdAt").setHeader("Zaman");

        grid.addComponentColumn(analytics -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteAnalytics(analytics));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return deleteButton;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        TextField playerIdField = new TextField("Oyuncu ID");
        TextField eventNameField = new TextField("Olay Adı (Event Name)");
        IntegerField levelField = new IntegerField("Seviye");
        IntegerField scoreField = new IntegerField("Skor");
        IntegerField coinField = new IntegerField("Altın");
        NumberField playTimeField = new NumberField("Oynama Süresi (sn)");

        Button saveButton = new Button("Kaydet", e -> {
            Analytics analytics = new Analytics();
            analytics.setPlayerId(playerIdField.getValue());
            analytics.setEventName(eventNameField.getValue());
            analytics.setLevel(levelField.getValue());
            analytics.setScore(scoreField.getValue());
            analytics.setCoinCount(coinField.getValue());
            analytics.setPlayTime(playTimeField.getValue());

            gameApiClient.saveAnalytics(analytics);
            Notification.show("Analitik başarıyla eklendi!");
            dialog.close();
            loadData();
        });

        VerticalLayout dialogLayout = new VerticalLayout(
                playerIdField, eventNameField, levelField, scoreField, coinField, playTimeField, saveButton
        );
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteAnalytics(Analytics analytics) {
        // gameApiClient içinde silme metodu eklendiğinde burası güncellenecektir
        Notification.show("Analitik kaydı silme işlemi yapılandırılıyor.");
        loadData();
    }

    private void loadData() {
        try {
            grid.setItems(gameApiClient.getAllAnalytics());
        } catch (Exception e) {
            Notification.show("Analitik verileri yüklenirken hata: " + e.getMessage());
        }
    }
}