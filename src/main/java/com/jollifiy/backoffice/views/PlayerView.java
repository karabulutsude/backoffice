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

import java.time.format.DateTimeFormatter;

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

        // Otomatik kolonları temizleyip özel formatlı kolonları ekliyoruz
        grid.removeAllColumns();

        grid.addColumn(Player::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Player::getPlayerId).setHeader("Oyuncu ID");
        grid.addColumn(Player::getDeviceId).setHeader("Cihaz ID");
        grid.addColumn(Player::getCountry).setHeader("Ülke");

        // Kayıt tarihini okunaklı formata çeviriyoruz
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        grid.addColumn(player -> player.getCreatedAt() != null ? player.getCreatedAt().format(formatter) : "")
                .setHeader("Kayıt Tarihi")
                .setSortable(true);

        // İşlemler kolonuna Düzenle ve Sil butonları eklendi
        grid.addComponentColumn(player -> {
            Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(player));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);

            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deletePlayer(player));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            HorizontalLayout actionsLayout = new HorizontalLayout(editButton, deleteButton);
            actionsLayout.setSpacing(false);
            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        refreshGrid();
    }

    private void openEditDialog(Player player) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Oyuncu Ülke Bilgisini Düzenle");

        TextField playerIdField = new TextField("Oyuncu ID (Değiştirilemez)");
        playerIdField.setValue(player.getPlayerId() != null ? player.getPlayerId() : "");
        playerIdField.setEnabled(false);
        playerIdField.setWidthFull();

        TextField deviceIdField = new TextField("Cihaz ID (Değiştirilemez)");
        deviceIdField.setValue(player.getDeviceId() != null ? player.getDeviceId() : "");
        deviceIdField.setEnabled(false);
        deviceIdField.setWidthFull();

        TextField countryField = new TextField("Ülke");
        countryField.setValue(player.getCountry() != null ? player.getCountry() : "");
        countryField.setWidthFull();

        Button saveButton = new Button("Güncelle", e -> {
            try {
                // Sadece ülke bilgisi güncelleniyor
                player.setCountry(countryField.getValue());

                backofficeService.updatePlayer(player.getId(), player);

                Notification notif = Notification.show("Oyuncu ülkesi başarıyla güncellendi!", 3000, Notification.Position.TOP_CENTER);
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