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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

        // Otomatik kolonları temizleyip özel formatlı kolonları ekliyoruz
        grid.removeAllColumns();

        grid.addColumn(Analytics::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Analytics::getPlayerId).setHeader("Oyuncu ID");
        grid.addColumn(Analytics::getEventName).setHeader("Olay Adı");
        grid.addColumn(Analytics::getLevel).setHeader("Seviye");
        grid.addColumn(Analytics::getScore).setHeader("Skor");
        grid.addColumn(Analytics::getCoinCount).setHeader("Altın");

        // Oynama süresi virgülden sonra 2 basamak olacak şekilde formatlanıyor (Örn: 11.10)
        grid.addColumn(analytics -> analytics.getPlayTime() != null
                        ? String.format(Locale.US, "%.2f", analytics.getPlayTime()) : "0.00")
                .setHeader("Oynama Süresi")
                .setSortable(true);

        // Zaman (createdAt) okunaklı formata çevriliyor
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        grid.addColumn(analytics -> analytics.getCreatedAt() != null ? analytics.getCreatedAt().format(formatter) : "")
                .setHeader("Zaman")
                .setSortable(true);

        // Çift tıklama kaldırıldı, işlemler kolonuna Düzenle (detay görüntüleme/read-only) ve Sil butonları eklendi
        grid.addComponentColumn(analytics -> {
            Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openDetailsDialog(analytics));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);

            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteAnalytics(analytics));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            HorizontalLayout actionsLayout = new HorizontalLayout(editButton, deleteButton);
            actionsLayout.setSpacing(false);
            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private void openDetailsDialog(Analytics analytics) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Analitik Kaydı Detayları (Salt Okunur)");

        TextField playerIdField = new TextField("Oyuncu ID");
        playerIdField.setValue(analytics.getPlayerId() != null ? analytics.getPlayerId() : "");
        playerIdField.setEnabled(false);
        playerIdField.setWidthFull();

        TextField eventNameField = new TextField("Olay Adı (Event Name)");
        eventNameField.setValue(analytics.getEventName() != null ? analytics.getEventName() : "");
        eventNameField.setEnabled(false);
        eventNameField.setWidthFull();

        TextField levelField = new TextField("Seviye");
        levelField.setValue(analytics.getLevel() != null ? analytics.getLevel().toString() : "");
        levelField.setEnabled(false);
        levelField.setWidthFull();

        TextField scoreField = new TextField("Skor");
        scoreField.setValue(analytics.getScore() != null ? analytics.getScore().toString() : "");
        scoreField.setEnabled(false);
        scoreField.setWidthFull();

        TextField coinField = new TextField("Altın");
        coinField.setValue(analytics.getCoinCount() != null ? analytics.getCoinCount().toString() : "");
        coinField.setEnabled(false);
        coinField.setWidthFull();

        TextField playTimeField = new TextField("Oynama Süresi (sn)");
        playTimeField.setValue(analytics.getPlayTime() != null ? String.format(Locale.US, "%.2f", analytics.getPlayTime()) : "0.00");
        playTimeField.setEnabled(false);
        playTimeField.setWidthFull();

        Button closeButton = new Button("Kapat", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttonsLayout = new HorizontalLayout(closeButton);

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