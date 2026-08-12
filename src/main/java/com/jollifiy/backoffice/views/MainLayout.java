package com.jollifiy.backoffice.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

public final class MainLayout extends AppLayout {

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(createApplicationHeader(), createApplicationDrawer(), createApplicationFooter());
    }

    private Component createApplicationHeader() {
        var appLogo = new Avatar("Game Analytics");
        appLogo.addClassName("app-logo");

        var appName = new Span("Game Analytics");
        appName.addClassName("app-name");

        var header = new HorizontalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);
        return header;
    }

    private Component createApplicationDrawer() {
        var scroller = new Scroller(createSideNav());
        return scroller;
    }

    private Component createApplicationFooter() {
        var footer = new VerticalLayout(new Span("Jollify Analytics Staj Projesi"));
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.addClassName("app-footer");
        return footer;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.setMinWidth(200, Unit.PIXELS);

        // Menü elemanlarının tümü:
        nav.addItem(new SideNavItem("Ana Sayfa", DashboardView.class, VaadinIcon.HOME.create()));
        nav.addItem(new SideNavItem("Oyuncular", PlayerView.class, VaadinIcon.USERS.create()));
        nav.addItem(new SideNavItem("Analitikler", AnalyticsView.class, VaadinIcon.CHART.create()));
        nav.addItem(new SideNavItem("İlerlemeler", ProgressView.class, VaadinIcon.TROPHY.create()));
        nav.addItem(new SideNavItem("Konfigürasyon", ConfigView.class, VaadinIcon.COG.create()));

        return nav;
    }
}