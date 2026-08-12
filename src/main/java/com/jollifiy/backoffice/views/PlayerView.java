package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.Player;
import com.jollifiy.backoffice.service.GameApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

@PageTitle("Oyuncular | Jollify Game Analytics")
@Route(value = "players", layout = MainLayout.class)
@PermitAll
public class PlayerView extends VerticalLayout {

    private final GameApiClient gameApiClient;
    private final Grid<Player> grid = new Grid<>(Player.class);

    @Autowired
    public PlayerView(GameApiClient gameApiClient) {
        this.gameApiClient = gameApiClient;
        setSizeFull();
        setPadding(true);

        add(new H2("Oyuncu Listesi"));

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> refreshGrid());
        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.setWidthFull();

        grid.setColumns("id", "playerId", "deviceId", "country", "createdAt");

        grid.getColumnByKey("id").setHeader("ID");
        grid.getColumnByKey("playerId").setHeader("Oyuncu ID");
        grid.getColumnByKey("deviceId").setHeader("Cihaz ID");
        grid.getColumnByKey("country").setHeader("Ülke");
        grid.getColumnByKey("createdAt").setHeader("Kayıt Tarihi");

        grid.setSizeFull();

        add(toolbar, grid);
        refreshGrid();
    }

    private void refreshGrid() {
        try {
            grid.setItems(gameApiClient.getAllPlayers());
        } catch (Exception e) {
            Notification.show("Oyuncu verileri yüklenirken hata: " + e.getMessage());
        }
    }
}