package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.AppConfig;
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

@PageTitle("Uygulama Konfigürasyonları | Jollify Game Analytics")
@Route(value = "config", layout = MainLayout.class)
@PermitAll
public class ConfigView extends VerticalLayout {

    private final BackofficeService backofficeService;
    private final Grid<AppConfig> grid = new Grid<>(AppConfig.class);

    @Autowired
    public ConfigView(BackofficeService backofficeService) {
        this.backofficeService = backofficeService;

        setSizeFull();
        setPadding(true);

        add(new H2("Uygulama Konfigürasyonları"));

        Button addButton = new Button("Yeni Konfigürasyon Ekle", VaadinIcon.PLUS.create(), e -> openAddDialog());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> loadData());

        HorizontalLayout toolbar = new HorizontalLayout(addButton, refreshButton);
        toolbar.setWidthFull();

        grid.setColumns("id", "configKey", "configValue");

        if (grid.getColumnByKey("id") != null) grid.getColumnByKey("id").setHeader("ID");
        if (grid.getColumnByKey("configKey") != null) grid.getColumnByKey("configKey").setHeader("Ayar Anahtarı (Key)");
        if (grid.getColumnByKey("configValue") != null) grid.getColumnByKey("configValue").setHeader("Ayar Değeri (Value)");

        // Satıra çift tıklandığında düzenleme penceresini aç
        grid.addItemDoubleClickListener(event -> openEditDialog(event.getItem()));

        // Silme butonu kolonu
        grid.addComponentColumn(config -> {
            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteConfig(config));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
            return deleteButton;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Yeni Konfigürasyon Ekle");

        TextField keyField = new TextField("Ayar Anahtarı (Key)");
        keyField.setWidthFull();

        TextField valueField = new TextField("Ayar Değeri (Value)");
        valueField.setWidthFull();

        Button saveButton = new Button("Kaydet", e -> {
            try {
                if (keyField.isEmpty() || valueField.isEmpty()) {
                    Notification.show("Lütfen tüm alanları doldurun!", 3000, Notification.Position.TOP_CENTER);
                    return;
                }

                AppConfig newConfig = new AppConfig();
                newConfig.setConfigKey(keyField.getValue());
                newConfig.setConfigValue(valueField.getValue());

                backofficeService.saveConfig(newConfig);

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
        VerticalLayout dialogLayout = new VerticalLayout(keyField, valueField, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void openEditDialog(AppConfig config) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Konfigürasyonu Düzenle");

        TextField keyField = new TextField("Ayar Anahtarı (Key)");
        keyField.setValue(config.getConfigKey() != null ? config.getConfigKey() : "");
        keyField.setWidthFull();

        TextField valueField = new TextField("Ayar Değeri (Value)");
        valueField.setValue(config.getConfigValue() != null ? config.getConfigValue() : "");
        valueField.setWidthFull();

        Button saveButton = new Button("Güncelle", e -> {
            try {
                config.setConfigKey(keyField.getValue());
                config.setConfigValue(valueField.getValue());

                backofficeService.updateConfig(config.getId(), config);

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
        VerticalLayout dialogLayout = new VerticalLayout(keyField, valueField, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteConfig(AppConfig config) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Konfigürasyonu Sil");
        confirmDialog.add("Bu konfigürasyon kaydını silmek istediğinizden emin misiniz?");

        Button confirmButton = new Button("Sil", e -> {
            try {
                backofficeService.deleteConfig(config.getId());
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
            grid.setItems(backofficeService.getAllConfigs());
        } catch (Exception e) {
            Notification.show("Konfigürasyon verileri yüklenirken hata: " + e.getMessage());
        }
    }
}