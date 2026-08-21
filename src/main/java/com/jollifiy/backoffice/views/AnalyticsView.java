package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Analytics;
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
import java.util.Locale;

@PageTitle("Oyun Analitikleri | Jollify Game Analytics")
@Route(value = "analytics", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ADMIN", "ANALYTICS", "ANALITIK", "ANALİTİK"})
public class AnalyticsView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<Analytics> grid = new Grid<>(Analytics.class);

    // Filtreleme bileşenleri (Sadece seçilebilir Select yapısı)
    private final Select<String> eventFilter = new Select<>();
    private final Select<String> timeFilter = new Select<>();

    @Autowired
    public AnalyticsView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;

        setSizeFull();
        setPadding(true);

        add(new H2("Oyun Analitikleri ve Olaylar"));

        // Olay Adı Filtresi Ayarları
        eventFilter.setLabel("Olay Adı Filtresi");
        eventFilter.setItems("Tümü", "GameScenes_WIN", "GameScenes_LOSE", "Level2_WIN", "Level2_LOSE", "Level3_WIN", "Level3_LOSE");
        eventFilter.setValue("Tümü");
        eventFilter.addValueChangeListener(e -> applyFilters());

        // Zaman Filtresi Ayarları
        timeFilter.setLabel("Zaman Aralığı");
        timeFilter.setItems("Tüm Zamanlar", "Bugün", "Son 7 Gün", "Bu Ay");
        timeFilter.setValue("Tüm Zamanlar");
        timeFilter.addValueChangeListener(e -> applyFilters());

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> {
            eventFilter.setValue("Tümü");
            timeFilter.setValue("Tüm Zamanlar");
            applyFilters();
        });

        HorizontalLayout toolbar = new HorizontalLayout(eventFilter, timeFilter, refreshButton);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
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

        // Kullanıcının silme yetkisini kontrol ediyoruz
        boolean hasDeletePermission = hasAnalyticsDeletePermission();

        grid.addComponentColumn(analytics -> {
            Button detailButton = new Button(VaadinIcon.EYE.create(), e -> openDetailsDialog(analytics));
            detailButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            detailButton.setTooltipText("Detayları Görüntüle");

            HorizontalLayout actionsLayout = new HorizontalLayout(detailButton);

            // Eğer kullanıcının analitik silme yetkisi varsa silme butonunu ekliyoruz
            if (hasDeletePermission) {
                Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteAnalytics(analytics));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                deleteButton.setTooltipText("Kayıt Sil");
                actionsLayout.add(deleteButton);
            }

            actionsLayout.setSpacing(false);
            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        applyFilters();
    }

    /**
     * Oturum açan kullanıcının analitik silme yetkisine sahip olup olmadığını kontrol eder.
     */
    private boolean hasAnalyticsDeletePermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;

        // Admin veya tam yetkili roller doğrudan silebilir
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        if (isAdmin) return true;

        // Özel yetkiler arasında ANALYTICS_DELETE veya ANALITIK_SILME var mı kontrol ediyoruz
        return auth.getAuthorities().stream()
                .anyMatch(a -> {
                    String authority = a.getAuthority().toUpperCase();
                    return authority.equals("ANALYTICS_DELETE") || authority.equals("ANALITIK_SILME") || authority.equals("ANALİTİK_SİLME");
                });
    }

    private void openDetailsDialog(Analytics analytics) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Analitik Kaydı Detay Kartı");
        dialog.setWidth("650px");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedDate = analytics.getCreatedAt() != null ? analytics.getCreatedAt().format(formatter) : "Bilinmiyor";

        VerticalLayout content = new VerticalLayout(
                createDetailRow("Veritabanı ID:", String.valueOf(analytics.getId())),
                createDetailRow("Oyuncu UUID:", analytics.getPlayerId() != null ? analytics.getPlayerId() : "-"),
                createDetailRow("Olay Adı (Event):", analytics.getEventName() != null ? analytics.getEventName() : "-"),
                createDetailRow("Seviye:", analytics.getLevel() != null ? analytics.getLevel().toString() : "-"),
                createDetailRow("Skor:", analytics.getScore() != null ? analytics.getScore().toString() : "-"),
                createDetailRow("Altın Sayısı:", analytics.getCoinCount() != null ? analytics.getCoinCount().toString() : "-"),
                createDetailRow("Oynama Süresi:", analytics.getPlayTime() != null ? String.format(Locale.US, "%.2f sn", analytics.getPlayTime()) : "0.00 sn"),
                createDetailRow("Zaman Damgası:", formattedDate)
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
        String selectedEvent = eventFilter.getValue();
        String selectedTime = timeFilter.getValue();

        try {
            List<Analytics> allAnalytics = backofficeService.getAllAnalytics();
            LocalDateTime now = LocalDateTime.now();

            List<Analytics> filteredList = allAnalytics.stream().filter(a -> {
                // Olay Adı Filtresi
                boolean matchesEvent = ("Tümü".equals(selectedEvent) || selectedEvent == null ||
                        (a.getEventName() != null && a.getEventName().equals(selectedEvent)));

                // Zaman Filtresi
                boolean matchesTime = true;
                if (a.getCreatedAt() != null && selectedTime != null) {
                    switch (selectedTime) {
                        case "Bugün":
                            matchesTime = a.getCreatedAt().toLocalDate().isEqual(LocalDate.now());
                            break;
                        case "Son 7 Gün":
                            matchesTime = a.getCreatedAt().isAfter(now.minusDays(7));
                            break;
                        case "Bu Ay":
                            matchesTime = a.getCreatedAt().getYear() == now.getYear() &&
                                    a.getCreatedAt().getMonth() == now.getMonth();
                            break;
                        default:
                            matchesTime = true; // "Tüm Zamanlar"
                    }
                }

                return matchesEvent && matchesTime;
            }).toList();

            grid.setItems(filteredList);
        } catch (Exception e) {
            Notification.show("Filtreleme hatası: " + e.getMessage());
        }
    }
}