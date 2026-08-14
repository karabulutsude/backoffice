package com.jollifiy.backoffice.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Game Analytics");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM);

        HorizontalLayout headerLayout = new HorizontalLayout(new DrawerToggle(), logo);
        headerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerLayout.setWidthFull();

        addToNavbar(headerLayout);
    }

    private void createDrawer() {
        VerticalLayout menuLayout = new VerticalLayout();
        menuLayout.setPadding(true);
        menuLayout.setSpacing(true);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (hasAuthority(authentication, "DASHBOARD")) {
            menuLayout.add(createMenuItem("Ana Sayfa", VaadinIcon.HOME, DashboardView.class));
        }
        if (hasAuthority(authentication, "PLAYERS")) {
            menuLayout.add(createMenuItem("Oyuncular", VaadinIcon.USERS, PlayerView.class));
        }
        if (hasAuthority(authentication, "ANALYTICS")) {
            menuLayout.add(createMenuItem("Analitikler", VaadinIcon.CHART, AnalyticsView.class));
        }
        if (hasAuthority(authentication, "PROGRESS")) {
            menuLayout.add(createMenuItem("İlerlemeler", VaadinIcon.TROPHY, ProgressView.class));
        }
        if (hasAuthority(authentication, "CONFIG")) {
            menuLayout.add(createMenuItem("Konfigürasyon", VaadinIcon.COG, ConfigView.class));
        }
        if (hasAuthority(authentication, "USERS")) {
            menuLayout.add(createMenuItem("Kullanıcılar", VaadinIcon.USER_CHECK, UserView.class));
        }

        String currentUsername = "Bilinmeyen Kullanıcı";
        if (authentication != null && authentication.isAuthenticated()) {
            currentUsername = authentication.getName();
        }

        Avatar avatar = new Avatar(currentUsername);
        avatar.setThemeName("xsmall");

        Span userName = new Span(currentUsername);
        userName.addClassName(LumoUtility.FontSize.SMALL);

        String roleText = "Yetkili Kullanıcı";
        if (hasAuthority(authentication, "ROLE_ADMIN")) {
            roleText = "Yönetici (ADMIN)";
        }
        Span userRole = new Span(roleText);
        userRole.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        VerticalLayout userInfoText = new VerticalLayout(userName, userRole);
        userInfoText.setPadding(false);
        userInfoText.setSpacing(false);

        HorizontalLayout userFooter = new HorizontalLayout(avatar, userInfoText);
        userFooter.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        userFooter.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Border.TOP, LumoUtility.BorderColor.CONTRAST_10);
        userFooter.setWidthFull();

        VerticalLayout drawerLayout = new VerticalLayout(menuLayout, userFooter);
        drawerLayout.setSizeFull();
        drawerLayout.setPadding(false);
        drawerLayout.setSpacing(false);
        drawerLayout.expand(menuLayout);

        addToDrawer(drawerLayout);
    }

    private boolean hasAuthority(Authentication authentication, String moduleKey) {
        if (authentication == null) {
            return false;
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN") || a.getAuthority().equalsIgnoreCase("ADMIN"));
        if (isAdmin) {
            return true;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", "").toUpperCase())
                .anyMatch(cleanAuth -> {
                    String target = moduleKey.toUpperCase();

                    if (target.equals("CONFIG") && (cleanAuth.contains("CONFIG") || cleanAuth.contains("KONFIGURASYON") || cleanAuth.contains("KONFİGÜRASYON"))) return true;
                    if (target.equals("ANALYTICS") && (cleanAuth.contains("ANALYTICS") || cleanAuth.contains("ANALITIK") || cleanAuth.contains("ANALİTİK"))) return true;
                    if (target.equals("PLAYERS") && (cleanAuth.contains("PLAYERS") || cleanAuth.contains("OYUNCU"))) return true;
                    if (target.equals("PROGRESS") && (cleanAuth.contains("PROGRESS") || cleanAuth.contains("ILERLEME") || cleanAuth.contains("İLERLEME"))) return true;
                    if (target.equals("USERS") && (cleanAuth.contains("USERS") || cleanAuth.contains("KULLANICI"))) return true;
                    if (target.equals("DASHBOARD")) return true;

                    return cleanAuth.equals(target) || cleanAuth.contains(target);
                });
    }

    private RouterLink createMenuItem(String text, VaadinIcon iconEnum, Class<? extends Component> navigationTarget) {
        Icon icon = iconEnum.create();
        icon.addClassName(LumoUtility.Margin.Right.SMALL);

        RouterLink link = new RouterLink(navigationTarget);
        link.add(icon, new Span(text));
        link.addClassNames(
                LumoUtility.Display.FLEX,
                LumoUtility.AlignItems.CENTER,
                LumoUtility.TextColor.BODY,
                LumoUtility.FontWeight.MEDIUM
        );
        return link;
    }
}