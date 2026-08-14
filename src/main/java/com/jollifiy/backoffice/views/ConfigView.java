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

        // Otomatik kolonları temizleyip özel formatlı kolonları ekliyoruz
        grid.removeAllColumns();

        grid.addColumn(AppConfig::getId).setHeader("ID").setSortable(true);
        grid.addColumn(AppConfig::getConfigKey).setHeader("Ayar Anahtarı (Key)").setSortable(true);
        grid.addColumn(AppConfig::getConfigValue).setHeader("Ayar Değeri (Value)").setSortable(true);

        // İşlemler kolonuna Düzenle ve Sil butonları eklendi
        grid.addComponentColumn(appConfig -> {
            Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(appConfig));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);

            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteConfig(appConfig));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            HorizontalLayout actionsLayout = new HorizontalLayout(editButton, deleteButton);
            actionsLayout.setSpacing(false);
            return actionsLayout;
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
                AppConfig appConfig = new AppConfig();
                appConfig.setConfigKey(keyField.getValue());
                appConfig.setConfigValue(valueField.getValue());

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

        VerticalLayout dialogLayout = new VerticalLayout(keyField, valueField, buttonsLayout);
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

        Button saveButton = new Button("Güncelle", e -> {
            try {
                appConfig.setConfigKey(keyField.getValue());
                appConfig.setConfigValue(valueField.getValue());

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

        VerticalLayout dialogLayout = new VerticalLayout(keyField, valueField, buttonsLayout);
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
            grid.setItems(backofficeService.getAllConfigs());
        } catch (Exception e) {
            Notification.show("Konfigürasyon verileri yüklenirken hata: " + e.getMessage());
        }
    }
}