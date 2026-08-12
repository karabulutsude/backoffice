package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.AppConfig;
import com.jollifiy.backoffice.service.GameApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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

    private final GameApiClient gameApiClient;
    private final Grid<AppConfig> grid = new Grid<>(AppConfig.class);

    @Autowired
    public ConfigView(GameApiClient gameApiClient) {
        this.gameApiClient = gameApiClient;

        setSizeFull();
        setPadding(true);

        add(new H2("Uygulama Konfigürasyonları"));

        Button addButton = new Button("Yeni Konfigürasyon Ekle", VaadinIcon.PLUS.create(), e -> openAddDialog());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> loadData());

        HorizontalLayout toolbar = new HorizontalLayout(addButton, refreshButton);
        toolbar.setWidthFull();

        grid.setColumns("id", "configKey", "configValue");

        grid.getColumnByKey("id").setHeader("ID");
        grid.getColumnByKey("configKey").setHeader("Ayar Anahtarı (Key)");
        grid.getColumnByKey("configValue").setHeader("Ayar Değeri (Value)");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        TextField keyField = new TextField("Ayar Anahtarı (Key)");
        TextField valueField = new TextField("Ayar Değeri (Value)");

        Button saveButton = new Button("Kaydet", e -> {
            AppConfig config = new AppConfig();
            config.setConfigKey(keyField.getValue());
            config.setConfigValue(valueField.getValue());

            gameApiClient.saveConfig(config);
            Notification.show("Konfigürasyon başarıyla eklendi!");
            dialog.close();
            loadData();
        });

        VerticalLayout dialogLayout = new VerticalLayout(keyField, valueField, saveButton);
        dialog.add(dialogLayout);
        dialog.open();
    }

    private void loadData() {
        try {
            grid.setItems(gameApiClient.getAllConfigs());
        } catch (Exception e) {
            Notification.show("Konfigürasyon verileri yüklenirken hata: " + e.getMessage());
        }
    }
}