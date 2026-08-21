package com.jollifiy.backoffice.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Erişim Engellendi")
@Route(value = "access-denied") // Şimdilik layout vermeden doğrudan test edelim
public class AccessDeniedView extends VerticalLayout {

    public AccessDeniedView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setSpacing(true);

        Icon icon = VaadinIcon.LOCK.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "var(--lumo-error-text-color)");

        H1 title = new H1("Erişim Engellendi");
        title.getStyle().set("margin", "0");

        Paragraph description = new Paragraph("Bu sayfayı görüntüleme yetkiniz bulunmamaktadır.");

        Button homeButton = new Button("Ana Sayfaya Dön", VaadinIcon.HOME.create(), e -> {
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(icon, title, description, homeButton);
    }
}