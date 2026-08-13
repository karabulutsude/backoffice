package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Progress;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Oyuncu İlerleme Durumları | Jollify Game Analytics")
@Route(value = "progress", layout = MainLayout.class)
@PermitAll
public class ProgressView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<Progress> grid = new Grid<>(Progress.class);

    @Autowired
    public ProgressView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;

        setSizeFull();
        setPadding(true);

        add(new H2("Oyuncu İlerleme Durumları"));

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> loadData());
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.setWidthFull();

        grid.setColumns("id", "playerId", "currentLevel", "totalCoins", "updatedAt");

        grid.getColumnByKey("id").setHeader("ID");
        grid.getColumnByKey("playerId").setHeader("Oyuncu ID");
        grid.getColumnByKey("currentLevel").setHeader("Mevcut Seviye");
        grid.getColumnByKey("totalCoins").setHeader("Toplam Altın");
        grid.getColumnByKey("updatedAt").setHeader("Güncellenme Zamanı");

        // Satıra çift tıklandığında düzenleme penceresini aç
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));

        // Silme butonu kolonu
        grid.addComponentColumn(progress -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteProgress(progress));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return deleteButton;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private void openEditDialog(Progress progress) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("İlerleme Durumunu Düzenle");

        TextField playerIdField = new TextField("Oyuncu ID");
        playerIdField.setValue(progress.getPlayerId() != null ? progress.getPlayerId() : "");
        playerIdField.setWidthFull();

        IntegerField levelField = new IntegerField("Mevcut Seviye");
        levelField.setValue(progress.getCurrentLevel());
        levelField.setWidthFull();

        IntegerField coinsField = new IntegerField("Toplam Altın");
        coinsField.setValue(progress.getTotalCoins());
        coinsField.setWidthFull();

        Button saveButton = new Button("Güncelle", e -> {
            try {
                progress.setPlayerId(playerIdField.getValue());
                progress.setCurrentLevel(levelField.getValue());
                progress.setTotalCoins(coinsField.getValue());

                backofficeService.updateProgress(progress.getId(), progress);

                Notification notif = Notification.show("İlerleme durumu başarıyla güncellendi!", 3000, Notification.Position.TOP_CENTER);
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
                playerIdField, levelField, coinsField, buttonsLayout
        );
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteProgress(Progress progress) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("İlerleme Kaydını Sil");
        confirmDialog.add("Bu ilerleme kaydını silmek istediğinizden emin misiniz?");

        Button confirmButton = new Button("Sil", e -> {
            try {
                backofficeService.deleteProgress(progress.getId());
                Notification notif = Notification.show("İlerleme kaydı silindi.", 3000, Notification.Position.TOP_CENTER);
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
            grid.setItems(backofficeService.getAllProgress());
        } catch (Exception e) {
            Notification.show("İlerleme verileri yüklenirken hata: " + e.getMessage());
        }
    }
}