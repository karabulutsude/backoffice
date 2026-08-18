package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Player;
import com.jollifiy.backoffice.service.BackofficeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Oyuncular | Jollify Game Analytics")
@Route(value = "players", layout = MainLayout.class)
@PermitAll
public class PlayerView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<Player> grid = new Grid<>(Player.class);

    // Select bileşeni içine yazı yazılmasına izin vermez, sadece tıklayıp seçtirir.
    private final Select<String> countryFilter = new Select<>();
    private final Select<String> timeFilter = new Select<>();

    @Autowired
    public PlayerView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;
        setSizeFull();
        setPadding(true);

        add(new H2("Oyuncu Listesi"));

        // Ülke Filtresi Ayarları
        countryFilter.setLabel("Ülke Filtresi");
        countryFilter.setItems("Tümü", "TR", "US", "DE", "ES", "FR");
        countryFilter.setValue("Tümü");
        countryFilter.addValueChangeListener(e -> applyFilters());

        // Zaman Filtresi Ayarları
        timeFilter.setLabel("Zaman Aralığı");
        timeFilter.setItems("Tüm Zamanlar", "Bugün", "Son 7 Gün", "Bu Ay");
        timeFilter.setValue("Tüm Zamanlar");
        timeFilter.addValueChangeListener(e -> applyFilters());

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> {
            countryFilter.setValue("Tümü");
            timeFilter.setValue("Tüm Zamanlar");
            refreshGrid();
        });

        HorizontalLayout toolbar = new HorizontalLayout(countryFilter, timeFilter, refreshButton);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.setWidthFull();

        grid.removeAllColumns();

        grid.addColumn(Player::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Player::getPlayerId).setHeader("Oyuncu ID");
        grid.addColumn(Player::getDeviceId).setHeader("Cihaz ID");
        grid.addColumn(Player::getCountry).setHeader("Ülke");

        grid.addColumn(player -> player.isBanned() ? "Engelli" : "Aktif")
                .setHeader("Durum")
                .setSortable(true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        grid.addColumn(player -> player.getCreatedAt() != null ? player.getCreatedAt().format(formatter) : "")
                .setHeader("Kayıt Tarihi")
                .setSortable(true);

        grid.addComponentColumn(player -> {
            Button detailButton = new Button(VaadinIcon.EYE.create(), e -> openDetailDialog(player));
            detailButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            detailButton.setTooltipText("Oyuncu Detayları");

            Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(player));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
            editButton.setTooltipText("Ülke Düzenle");

            Button banButton = new Button(player.isBanned() ? VaadinIcon.UNLOCK.create() : VaadinIcon.BAN.create(),
                    e -> toggleBanPlayer(player));
            banButton.addThemeVariants(player.isBanned() ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            banButton.setTooltipText(player.isBanned() ? "Engeli Kaldır" : "Oyuncuyu Engelle");

            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deletePlayer(player));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.setTooltipText("Oyuncuyu Sil");

            HorizontalLayout actionsLayout = new HorizontalLayout(detailButton, editButton, banButton, deleteButton);
            actionsLayout.setSpacing(false);
            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        refreshGrid();
    }

    private void openDetailDialog(Player player) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Oyuncu Detay Kartı");
        dialog.setWidth("650px");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDate = player.getCreatedAt() != null ? player.getCreatedAt().format(formatter) : "Bilinmiyor";
        String statusText = player.isBanned() ? "Engelli 🚫" : "Aktif ✅";

        VerticalLayout content = new VerticalLayout(
                createDetailRow("Veritabanı ID:", String.valueOf(player.getId())),
                createDetailRow("Oyuncu UUID:", player.getPlayerId() != null ? player.getPlayerId() : "-"),
                createDetailRow("Cihaz UUID:", player.getDeviceId() != null ? player.getDeviceId() : "-"),
                createDetailRow("Kayıt Ülkesi:", player.getCountry() != null ? player.getCountry() : "-"),
                createDetailRow("Hesap Durumu:", statusText),
                createDetailRow("Kayıt Zamanı:", formattedDate)
        );
        content.setSpacing(true);
        content.setPadding(false);

        Button closeButton = new Button("Kapat", e -> dialog.close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.add(content);
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    private HorizontalLayout createDetailRow(String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "bold");
        labelSpan.setWidth("140px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("word-break", "break-all");
        valueSpan.getStyle().set("flex-grow", "1");

        HorizontalLayout row = new HorizontalLayout(labelSpan, valueSpan);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        return row;
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
                player.setCountry(countryField.getValue());
                backofficeService.updatePlayer(player.getId(), player);

                Notification notif = Notification.show("Oyuncu ülkesi başarıyla güncellendi!", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                applyFilters();
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

    private void toggleBanPlayer(Player player) {
        boolean newBanStatus = !player.isBanned();
        String actionMessage = newBanStatus ? "Oyuncu engellendi." : "Oyuncunun engeli kaldırıldı.";

        try {
            player.setBanned(newBanStatus);
            backofficeService.updatePlayer(player.getId(), player);

            Notification notif = Notification.show(actionMessage, 3000, Notification.Position.TOP_CENTER);
            notif.addThemeVariants(newBanStatus ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);

            applyFilters();
        } catch (Exception ex) {
            Notification.show("İşlem hatası: " + ex.getMessage());
        }
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
                applyFilters();
            } catch (Exception ex) {
                Notification.show("Silme hatası: " + ex.getMessage());
            }
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("İptal", e -> confirmDialog.close());

        confirmDialog.getFooter().add(confirmButton, cancelBtn);
        confirmDialog.open();
    }

    private void applyFilters() {
        String country = countryFilter.getValue();
        String selectedTime = timeFilter.getValue();

        try {
            List<Player> allPlayers = backofficeService.getAllPlayers();
            LocalDateTime now = LocalDateTime.now();

            List<Player> filteredPlayers = allPlayers.stream().filter(p -> {
                // Ülke Filtresi Kontrolü
                boolean matchesCountry = ("Tümü".equals(country) || country == null ||
                        (p.getCountry() != null && p.getCountry().equalsIgnoreCase(country)));

                // Zaman Filtresi Kontrolü
                boolean matchesTime = true;
                if (p.getCreatedAt() != null && selectedTime != null) {
                    switch (selectedTime) {
                        case "Bugün":
                            matchesTime = p.getCreatedAt().toLocalDate().isEqual(LocalDate.now());
                            break;
                        case "Son 7 Gün":
                            matchesTime = p.getCreatedAt().isAfter(now.minusDays(7));
                            break;
                        case "Bu Ay":
                            matchesTime = p.getCreatedAt().getYear() == now.getYear() &&
                                    p.getCreatedAt().getMonth() == now.getMonth();
                            break;
                        default:
                            matchesTime = true; // "Tüm Zamanlar"
                    }
                }

                return matchesCountry && matchesTime;
            }).toList();

            grid.setItems(filteredPlayers);
        } catch (Exception e) {
            Notification.show("Filtreleme hatası: " + e.getMessage());
        }
    }

    private void refreshGrid() {
        applyFilters();
    }
}