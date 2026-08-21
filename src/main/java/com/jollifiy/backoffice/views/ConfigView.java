package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.AppConfig;
import com.jollifiy.backoffice.service.BackofficeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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

import java.util.ArrayList;
import java.util.List;

@PageTitle("Uygulama Konfigürasyonları | Jollify Game Analytics")
@Route(value = "config", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ADMIN", "CONFIG", "KONFIGURASYON", "KONFİGÜRASYON"})
public class ConfigView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<AppConfig> grid = new Grid<>(AppConfig.class);

    private final Select<String> configFilterSelect = new Select<>();

    @Autowired
    public ConfigView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;

        setSizeFull();
        setPadding(true);

        add(new H2("Uygulama Konfigürasyonları"));

        // Ayar Anahtarı Filtresi ("Tümü" seçeneği ile birlikte)
        configFilterSelect.setLabel("Ayar Anahtarı Filtresi");
        configFilterSelect.setWidth("250px");
        configFilterSelect.addValueChangeListener(e -> applyFilter());

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> {
            configFilterSelect.setValue("Tümü");
            loadData();
        });

        HorizontalLayout toolbar = new HorizontalLayout(configFilterSelect, refreshButton);
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        toolbar.setWidthFull();

        // Yeni Konfigürasyon Ekleme Yetkisi Kontrolü
        if (hasConfigAddPermission()) {
            Button addButton = new Button("Yeni Konfigürasyon Ekle", VaadinIcon.PLUS.create(), e -> openAddDialog());
            addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            toolbar.addComponentAtIndex(1, addButton);
        }

        grid.removeAllColumns();

        grid.addColumn(AppConfig::getId).setHeader("ID").setSortable(true);
        grid.addColumn(AppConfig::getConfigKey).setHeader("Ayar Anahtarı (Key)").setSortable(true);
        grid.addColumn(AppConfig::getConfigValue).setHeader("Ayar Değeri (Value)").setSortable(true);

        // Aktif / Pasif Durum Kolonu
        grid.addComponentColumn(appConfig -> {
            boolean isActive = appConfig.getIsActive() != null ? appConfig.getIsActive() : true;
            Span badge = new Span(isActive ? "Aktif" : "Pasif");
            badge.getElement().getThemeList().add(isActive ? "badge success" : "badge error");
            return badge;
        }).setHeader("Durum").setSortable(true);

        boolean hasEditPermission = hasConfigEditPermission();
        boolean hasDeletePermission = hasConfigDeletePermission();

        // İşlemler Kolonu
        grid.addComponentColumn(appConfig -> {
            Button detailButton = new Button(VaadinIcon.EYE.create(), e -> openDetailDialog(appConfig));
            detailButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            detailButton.setTooltipText("Konfigürasyon Detayları");

            HorizontalLayout actionsLayout = new HorizontalLayout(detailButton);

            if (hasEditPermission) {
                Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(appConfig));
                editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
                editButton.setTooltipText("Düzenle");
                actionsLayout.add(editButton);
            }

            if (hasDeletePermission) {
                Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteConfig(appConfig));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                deleteButton.setTooltipText("Sil");
                actionsLayout.add(deleteButton);
            }

            actionsLayout.setSpacing(false);
            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private boolean hasConfigAddPermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        if (isAdmin(auth)) return true;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String u = a.getAuthority().toUpperCase();
            return u.equals("CONFIG_ADD") || u.equals("KONFIG_EKLE") || u.equals("KONFIGÜRASYON_EKLE") || u.equals("KONFİGÜRASYON_EKLE");
        });
    }

    private boolean hasConfigEditPermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        if (isAdmin(auth)) return true;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String u = a.getAuthority().toUpperCase();
            return u.equals("CONFIG_EDIT") || u.equals("KONFIG_DUZENLE") || u.equals("KONFIGÜRASYON_DÜZENLE") || u.equals("KONFİGÜRASYON_DÜZENLE");
        });
    }

    private boolean hasConfigDeletePermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        if (isAdmin(auth)) return true;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String u = a.getAuthority().toUpperCase();
            return u.equals("CONFIG_DELETE") || u.equals("KONFIG_SILME") || u.equals("KONFIGÜRASYON_SİLME") || u.equals("KONFİGÜRASYON_SİLME");
        });
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private void openDetailDialog(AppConfig appConfig) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Konfigürasyon Detay Kartı");
        dialog.setWidth("500px");

        boolean isActive = appConfig.getIsActive() != null ? appConfig.getIsActive() : true;

        VerticalLayout content = new VerticalLayout(
                createDetailRow("ID:", String.valueOf(appConfig.getId())),
                createDetailRow("Ayar Anahtarı:", appConfig.getConfigKey() != null ? appConfig.getConfigKey() : "-"),
                createDetailRow("Ayar Değeri:", appConfig.getConfigValue() != null ? appConfig.getConfigValue() : "-"),
                createDetailRow("Durum:", isActive ? "Aktif" : "Pasif")
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
        labelSpan.setWidth("130px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("word-break", "break-all");
        valueSpan.getStyle().set("flex-grow", "1");

        HorizontalLayout row = new HorizontalLayout(labelSpan, valueSpan);
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        return row;
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Yeni Konfigürasyon Ekle");

        TextField keyField = new TextField("Ayar Anahtarı (Key)");
        keyField.setWidthFull();

        TextField valueField = new TextField("Ayar Değeri (Value)");
        valueField.setWidthFull();

        Checkbox activeCheckbox = new Checkbox("Aktif Et (Konfigürasyon Kullanımda Olsun)");
        activeCheckbox.setValue(true);

        Button saveButton = new Button("Kaydet", e -> {
            try {
                AppConfig appConfig = new AppConfig();
                appConfig.setConfigKey(keyField.getValue());
                appConfig.setConfigValue(valueField.getValue());
                appConfig.setIsActive(activeCheckbox.getValue());

                backofficeService.saveConfig(appConfig);

                Notification notif = Notification.show("Konfigürasyon başarıyla eklendi!", 3000, Notification.Position.TOP_CENTER);
                notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                loadData();
            } catch (Exception ex) {
                Notification.show("Ekleme hatası: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("İptal", e -> dialog.close());
        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout(keyField, valueField, activeCheckbox, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void openEditDialog(AppConfig appConfig) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Konfigürasyonu Düzenle");

        TextField keyField = new TextField("Ayar Anahtarı (Key)");
        keyField.setValue(appConfig.getConfigKey() != null ? appConfig.getConfigKey() : "");
        keyField.setWidthFull();

        TextField valueField = new TextField("Ayar Değeri (Value)");
        valueField.setValue(appConfig.getConfigValue() != null ? appConfig.getConfigValue() : "");
        valueField.setWidthFull();

        Checkbox activeCheckbox = new Checkbox("Aktif Et (Konfigürasyon Kullanımda Olsun)");
        activeCheckbox.setValue(appConfig.getIsActive() != null ? appConfig.getIsActive() : true);

        Button saveButton = new Button("Güncelle", e -> {
            try {
                appConfig.setConfigKey(keyField.getValue());
                appConfig.setConfigValue(valueField.getValue());
                appConfig.setIsActive(activeCheckbox.getValue());

                backofficeService.updateConfig(appConfig.getId(), appConfig);

                Notification notif = Notification.show("Konfigürasyon başarıyla güncellendi!", 3000, Notification.Position.TOP_CENTER);
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

        VerticalLayout dialogLayout = new VerticalLayout(keyField, valueField, activeCheckbox, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteConfig(AppConfig appConfig) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Konfigürasyonu Sil");
        confirmDialog.add("Bu konfigürasyonu silmek istediğinizden emin misiniz?");

        Button confirmButton = new Button("Sil", e -> {
            try {
                backofficeService.deleteConfig(appConfig.getId());
                Notification notif = Notification.show("Konfigürasyon silindi.", 3000, Notification.Position.TOP_CENTER);
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
            List<AppConfig> allConfigs = backofficeService.getAllConfigs();
            grid.setItems(allConfigs);

            List<String> keys = new ArrayList<>();
            keys.add("Tümü");

            allConfigs.stream()
                    .map(AppConfig::getConfigKey)
                    .filter(key -> key != null && !key.isEmpty())
                    .distinct()
                    .forEach(keys::add);

            configFilterSelect.setItems(keys);
            if (configFilterSelect.getValue() == null) {
                configFilterSelect.setValue("Tümü");
            }
        } catch (Exception e) {
            Notification.show("Konfigürasyon verileri yüklenirken hata: " + e.getMessage());
        }
    }

    private void applyFilter() {
        String selectedKey = configFilterSelect.getValue();
        try {
            List<AppConfig> allConfigs = backofficeService.getAllConfigs();
            if (selectedKey == null || "Tümü".equals(selectedKey)) {
                grid.setItems(allConfigs);
            } else {
                List<AppConfig> filtered = allConfigs.stream()
                        .filter(c -> selectedKey.equals(c.getConfigKey()))
                        .toList();
                grid.setItems(filtered);
            }
        } catch (Exception e) {
            Notification.show("Filtreleme hatası: " + e.getMessage());
        }
    }
}