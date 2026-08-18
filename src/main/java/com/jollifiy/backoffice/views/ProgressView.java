package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Progress;
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

import java.time.format.DateTimeFormatter;
import java.util.List;

@PageTitle("Oyuncu İlerlemeleri | Jollify Game Analytics")
@Route(value = "progress", layout = MainLayout.class)
@PermitAll
public class ProgressView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<Progress> grid = new Grid<>(Progress.class);

    private final Select<String> levelFilter = new Select<>();
    private final Select<String> timeFilter = new Select<>();

    @Autowired
    public ProgressView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;
        setSizeFull();
        setPadding(true);

        add(new H2("Oyuncu İlerleme Durumları"));

        // Seviye Filtresi
        levelFilter.setLabel("Seviye Filtresi");
        levelFilter.setItems("Tümü", "Seviye 1", "Seviye 2", "Seviye 3");
        levelFilter.setValue("Tümü");
        levelFilter.addValueChangeListener(e -> applyFilters());

        // Zaman Aralığı Filtresi
        timeFilter.setLabel("Zaman Aralığı");
        timeFilter.setItems("Tüm Zamanlar", "Bugün", "Son 7 Gün", "Bu Ay");
        timeFilter.setValue("Tüm Zamanlar");
        timeFilter.addValueChangeListener(e -> applyFilters());

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> {
            levelFilter.setValue("Tümü");
            timeFilter.setValue("Tüm Zamanlar");
            applyFilters();
        });

        // BURASI DÜZELTİLDİ: Filtreler toolbar içine eklendi
        HorizontalLayout toolbar = new HorizontalLayout(levelFilter, timeFilter, refreshButton);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.setWidthFull();

        grid.removeAllColumns();

        grid.addColumn(Progress::getId).setHeader("ID").setSortable(true);
        grid.addColumn(Progress::getPlayerId).setHeader("Oyuncu ID");
        grid.addColumn(Progress::getCurrentLevel).setHeader("Mevcut Seviye").setSortable(true);
        grid.addColumn(Progress::getTotalCoins).setHeader("Toplam Altın").setSortable(true);

        // Eğer Progress entity'sinde zaman alanı varsa (Örn: getUpdatedAt veya getCreatedAt)
        // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        // grid.addColumn(progress -> progress.getUpdatedAt() != null ? progress.getUpdatedAt().format(formatter) : "").setHeader("Güncelleme Zamanı");

        grid.addComponentColumn(progress -> {
            Button detailButton = new Button(VaadinIcon.EYE.create(), e -> openDetailDialog(progress));
            detailButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            detailButton.setTooltipText("İlerleme Detayları");

            Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(progress));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
            editButton.setTooltipText("Seviye/Altın Düzenle");

            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteProgress(progress));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            deleteButton.setTooltipText("Kaydı Sil");

            HorizontalLayout actionsLayout = new HorizontalLayout(detailButton, editButton, deleteButton);
            actionsLayout.setSpacing(false);
            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();
        add(toolbar, grid);
        applyFilters();
    }

    private void openDetailDialog(Progress progress) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("İlerleme Detay Kartı");
        dialog.setWidth("550px");

        VerticalLayout content = new VerticalLayout(
                createDetailRow("Veritabanı ID:", String.valueOf(progress.getId())),
                createDetailRow("Oyuncu UUID:", progress.getPlayerId() != null ? progress.getPlayerId() : "-"),
                createDetailRow("Mevcut Seviye:", progress.getCurrentLevel() != null ? progress.getCurrentLevel().toString() : "-"),
                createDetailRow("Toplam Altın:", progress.getTotalCoins() != null ? progress.getTotalCoins().toString() : "-")
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

    private void openEditDialog(Progress progress) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("İlerleme Bilgilerini Düzenle");
        dialog.setWidth("500px");

        // Oyuncu ID alanı eklendi ve salt okunur yapıldı
        TextField playerIdField = new TextField("Oyuncu ID");
        playerIdField.setValue(progress.getPlayerId() != null ? progress.getPlayerId() : "");
        playerIdField.setEnabled(false);
        playerIdField.setWidthFull();

        TextField levelField = new TextField("Mevcut Seviye");
        levelField.setValue(progress.getCurrentLevel() != null ? progress.getCurrentLevel().toString() : "");
        levelField.setWidthFull();

        TextField coinField = new TextField("Toplam Altın");
        coinField.setValue(progress.getTotalCoins() != null ? progress.getTotalCoins().toString() : "");
        coinField.setWidthFull();

        Button saveButton = new Button("Güncelle", e -> {
            try {
                progress.setCurrentLevel(Integer.parseInt(levelField.getValue()));
                progress.setTotalCoins(Integer.parseInt(coinField.getValue()));
                backofficeService.updateProgress(progress.getId(), progress);

                Notification notif = Notification.show("İlerleme başarıyla güncellendi!", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                applyFilters();
            } catch (Exception ex) {
                Notification.show("Güncelleme hatası: Lütfen sayısal değerler giriniz.");
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("İptal", e -> dialog.close());
        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);

        // playerIdField başa eklendi
        VerticalLayout dialogLayout = new VerticalLayout(playerIdField, levelField, coinField, buttonsLayout);
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
        String selectedLevel = levelFilter.getValue();

        try {
            List<Progress> allProgress = backofficeService.getAllProgress();

            List<Progress> filteredList = allProgress.stream().filter(p -> {
                if ("Tümü".equals(selectedLevel) || selectedLevel == null) {
                    return true;
                }
                String levelNumStr = selectedLevel.replace("Seviye ", "").trim();
                return p.getCurrentLevel() != null && p.getCurrentLevel().toString().equals(levelNumStr);
            }).toList();

            grid.setItems(filteredList);
        } catch (Exception e) {
            Notification.show("Filtreleme hatası: " + e.getMessage());
        }
    }
}