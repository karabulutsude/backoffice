package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Player;
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

@PageTitle("Oyuncular | Jollify Game Analytics")
@Route(value = "players", layout = MainLayout.class)
@PermitAll
public class PlayerView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<Player> grid = new Grid<>(Player.class);

    @Autowired
    public PlayerView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;
        setSizeFull();
        setPadding(true);

        add(new H2("Oyuncu Listesi"));

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> refreshGrid());
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.setWidthFull();

        grid.setColumns("id", "playerId", "deviceId", "country", "createdAt");

        grid.getColumnByKey("id").setHeader("ID");
        grid.getColumnByKey("playerId").setHeader("Oyuncu ID");
        grid.getColumnByKey("deviceId").setHeader("Cihaz ID");
        grid.getColumnByKey("country").setHeader("Ülke");
        grid.getColumnByKey("createdAt").setHeader("Kayıt Tarihi");

        // Satıra tıklandığında düzenleme penceresini aç
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));

        // Silme butonu kolonunu ekle
        grid.addComponentColumn(player -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deletePlayer(player));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return deleteButton;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        refreshGrid();
    }

    private void openEditDialog(Player player) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Oyuncu Bilgilerini Düzenle");

        TextField playerIdField = new TextField("Oyuncu ID");
        playerIdField.setValue(player.getPlayerId() != null ? player.getPlayerId() : "");
        playerIdField.setWidthFull();

        TextField deviceIdField = new TextField("Cihaz ID (Değiştirilemez)");
        deviceIdField.setValue(player.getDeviceId() != null ? player.getDeviceId() : "");
        deviceIdField.setEnabled(false); // Otomatik geldiği için değiştirilemez yapıldı
        deviceIdField.setWidthFull();

        TextField countryField = new TextField("Ülke");
        countryField.setValue(player.getCountry() != null ? player.getCountry() : "");
        countryField.setWidthFull();

        Button saveButton = new Button("Güncelle", e -> {
            try {
                player.setPlayerId(playerIdField.getValue());
                player.setCountry(countryField.getValue());

                backofficeService.updatePlayer(player.getId(), player);

                Notification notif = Notification.show("Oyuncu başarıyla güncellendi!", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Güncelleme hatası: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("İptal", e -> dialog.close());

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout(playerIdField, deviceIdField, countryField, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deletePlayer(Player player) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Oyuncuyu Sil");
        confirmDialog.add("Bu oyuncuyu silmek istediğinizden emin misiniz?");

        Button confirmButton = new Button("Sil", e -> {
            try {
                backofficeService.deletePlayer(player.getId());
                Notification notif = Notification.show("Oyuncu silindi.", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Silme hatası: " + ex.getMessage());
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("İptal", e -> confirmDialog.close());

        confirmDialog.getFooter().add(confirmButton, cancelBtn);
        confirmDialog.open();
    }

    private void refreshGrid() {
        try {
            grid.setItems(backofficeService.getAllPlayers());
        } catch (Exception e) {
            Notification.show("Oyuncu verileri yüklenirken hata: " + e.getMessage());
        }
    }
}