package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Analytics;
import com.jollifiy.backoffice.service.BackofficeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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

    private final BackofficeService backofficeService;
    private final Grid<Analytics> grid = new Grid<>(Analytics.class);

    @Autowired
    public AnalyticsView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;

        setSizeFull();
        setPadding(true);

        add(new H2("Oyun Analitikleri ve Olaylar"));

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> loadData());
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
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

        // Satıra çift tıklandığında düzenleme penceresini aç
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));

        // Silme butonu
        grid.addComponentColumn(analytics -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteAnalytics(analytics));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return deleteButton;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private void openEditDialog(Analytics analytics) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Analitik Kaydını Düzenle");

        TextField playerIdField = new TextField("Oyuncu ID");
        playerIdField.setValue(analytics.getPlayerId() != null ? analytics.getPlayerId() : "");
        playerIdField.setWidthFull();

        TextField eventNameField = new TextField("Olay Adı (Event Name)");
        eventNameField.setValue(analytics.getEventName() != null ? analytics.getEventName() : "");
        eventNameField.setWidthFull();

        IntegerField levelField = new IntegerField("Seviye");
        levelField.setValue(analytics.getLevel());
        levelField.setWidthFull();

        IntegerField scoreField = new IntegerField("Skor");
        scoreField.setValue(analytics.getScore());
        scoreField.setWidthFull();

        IntegerField coinField = new IntegerField("Altın");
        coinField.setValue(analytics.getCoinCount());
        coinField.setWidthFull();

        NumberField playTimeField = new NumberField("Oynama Süresi (sn)");
        playTimeField.setValue(analytics.getPlayTime() != null ? analytics.getPlayTime() : 0.0);
        playTimeField.setWidthFull();

        Button saveButton = new Button("Güncelle", e -> {
            try {
                analytics.setPlayerId(playerIdField.getValue());
                analytics.setEventName(eventNameField.getValue());
                analytics.setLevel(levelField.getValue());
                analytics.setScore(scoreField.getValue());
                analytics.setCoinCount(coinField.getValue());
                analytics.setPlayTime(playTimeField.getValue());

                backofficeService.updateAnalytics(analytics.getId(), analytics);

                Notification notif = Notification.show("Analitik başarıyla güncellendi!", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                loadData();
            } catch (Exception ex) {
                Notification.show("Güncelleme hatası: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("İptal", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout(
                playerIdField, eventNameField, levelField, scoreField, coinField, playTimeField, buttonsLayout
        );
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteAnalytics(Analytics analytics) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Analitik Kaydını Sil");
        confirmDialog.add("Bu analitik kaydını silmek istediğinizden emin misiniz?");

        Button confirmButton = new Button("Sil", e -> {
            try {
                backofficeService.deleteAnalytics(analytics.getId());
                Notification notif = Notification.show("Analitik kaydı silindi.", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                loadData();
            } catch (Exception ex) {
                Notification.show("Silme hatası: " + ex.getMessage());
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("İptal", e -> confirmDialog.close());

        confirmDialog.getFooter().add(confirmButton, cancelBtn);
        confirmDialog.open();
    }

    private void loadData() {
        try {
            grid.setItems(backofficeService.getAllAnalytics());
        } catch (Exception e) {
            Notification.show("Analitik verileri yüklenirken hata: " + e.getMessage());
        }
    }
}