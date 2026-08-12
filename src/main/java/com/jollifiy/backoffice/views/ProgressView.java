package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Progress;
import com.jollifiy.backoffice.service.GameApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("Oyuncu İlerleme Durumları | Jollify Game Analytics")
@Route(value = "progress", layout = MainLayout.class)
@PermitAll
public class ProgressView extends VerticalLayout {

    private final GameApiClient gameApiClient;
    private final Grid<Progress> grid = new Grid<>(Progress.class);

    @Autowired
    public ProgressView(GameApiClient gameApiClient) {
        this.gameApiClient = gameApiClient;

        setSizeFull();
        setPadding(true);

        add(new H2("Oyuncu İlerleme Durumları"));

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> loadData());
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.setWidthFull();

        grid.setColumns("id", "playerId", "currentLevel", "totalCoins", "updatedAt");

        grid.getColumnByKey("id").setHeader("ID");
        grid.getColumnByKey("playerId").setHeader("Oyuncu ID");
        grid.getColumnByKey("currentLevel").setHeader("Mevcut Seviye");
        grid.getColumnByKey("totalCoins").setHeader("Toplam Altın");
        grid.getColumnByKey("updatedAt").setHeader("Güncellenme Zamanı");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private void loadData() {
        try {
            grid.setItems(gameApiClient.getAllProgress());
        } catch (Exception e) {
            Notification.show("İlerleme verileri yüklenirken hata: " + e.getMessage());
        }
    }
}