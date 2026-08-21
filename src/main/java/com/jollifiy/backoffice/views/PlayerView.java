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
import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Oyuncular | Jollify Game Analytics")
@Route(value = "players", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ADMIN", "PLAYERS", "OYUNCU", "OYUNCULAR"})
public class PlayerView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<Player> grid = new Grid<>(Player.class);

    private final Select<String> countryFilter = new Select<>();
    private final Select<String> timeFilter = new Select<>();

    @Autowired
    public PlayerView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;
        setSizeFull();
        setPadding(true);

        add(new H2("Oyuncu Listesi"));

        countryFilter.setLabel("Ülke Filtresi");
        countryFilter.setItems("Tümü", "TR", "US", "DE", "ES", "FR");
        countryFilter.setValue("Tümü");
        countryFilter.addValueChangeListener(e -> applyFilters());

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

            HorizontalLayout actionsLayout = new HorizontalLayout(detailButton);
            actionsLayout.setSpacing(false);

            // Yetki kontrolleri (Admin veya ilgili işlem yetkisi var mı?)
            boolean isAdmin = hasAnyRole("ADMIN", "ROLE_ADMIN");
            boolean canEdit = isAdmin || hasAnyRole("PLAYER_EDIT", "OYUNCU DÜZENLE", "OYUNCU_DUZENLE");
            boolean canBan = isAdmin || hasAnyRole("PLAYER_BAN", "OYUNCU ENGELLEME", "OYUNCU_ENGELLE");
            boolean canDelete = isAdmin || hasAnyRole("PLAYER_DELETE", "OYUNCU SİLME", "OYUNCU_SIL");

            if (canEdit) {
                Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(player));
                editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
                editButton.setTooltipText("Ülke Düzenle");
                actionsLayout.add(editButton);
            }

            if (canBan) {
                Button banButton = new Button(player.isBanned() ? VaadinIcon.UNLOCK.create() : VaadinIcon.BAN.create(),
                        e -> toggleBanPlayer(player));
                banButton.addThemeVariants(player.isBanned() ? ButtonVariant.LUMO_SUCCESS : ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                banButton.setTooltipText(player.isBanned() ? "Engeli Kaldır" : "Oyuncuyu Engelle");
                actionsLayout.add(banButton);
            }

            if (canDelete) {
                Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deletePlayer(player));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                deleteButton.setTooltipText("Oyuncuyu Sil");
                actionsLayout.add(deleteButton);
            }

            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        refreshGrid();
    }

    private boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(authority -> {
                    String authName = authority.getAuthority().trim();
                    for (String role : roles) {
                        if (authName.equalsIgnoreCase(role) || authName.equalsIgnoreCase("ROLE_" + role)) {
                            return true;
                        }
                    }
                    return false;
                });
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

        TextField playerIdField = new TextField("Oyuncu ID");
        playerIdField.setValue(player.getPlayerId() != null ? player.getPlayerId() : "");
        playerIdField.setEnabled(false);
        playerIdField.setWidthFull();

        TextField deviceIdField = new TextField("Cihaz ID");
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

                Notification.show("Oyuncu ülkesi güncellendi!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                applyFilters();
            } catch (Exception ex) {
                Notification.show("Hata: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
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
        String actionMessage = newBanStatus ? "Oyuncu engellendi." : "Engel kaldırıldı.";

        try {
            player.setBanned(newBanStatus);
            backofficeService.updatePlayer(player.getId(), player);

            Notification.show(actionMessage, 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(newBanStatus ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);

            applyFilters();
        } catch (Exception ex) {
            Notification.show("Hata: " + ex.getMessage());
        }
    }

    private void deletePlayer(Player player) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Oyuncuyu Sil");
        confirmDialog.add("Bu oyuncuyu silmek istediğinizden emin misiniz?");

        Button confirmButton = new Button("Sil", e -> {
            try {
                backofficeService.deletePlayer(player.getId());
                Notification.show("Oyuncu silindi.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                confirmDialog.close();
                applyFilters();
            } catch (Exception ex) {
                Notification.show("Hata: " + ex.getMessage());
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
                boolean matchesCountry = ("Tümü".equals(country) || country == null ||
                        (p.getCountry() != null && p.getCountry().equalsIgnoreCase(country)));

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
                            matchesTime = true;
                    }
                }

                return matchesCountry && matchesTime;
            }).toList();

            grid.setItems(filteredPlayers);
        } catch (Exception e) {
            Notification.show("Hata: " + e.getMessage());
        }
    }

    private void refreshGrid() {
        applyFilters();
    }
}