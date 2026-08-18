package com.jollifiy.backoffice.views;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@PageTitle("Giriş Yap | Jollify Game Analytics")
@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();
    private final Span errorSpan = new Span();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setAction("login");

        // Hata mesajı için şık ve uyumlu stil ayarları
        errorSpan.getStyle().set("color", "var(--lumo-error-text-color)");
        errorSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
        errorSpan.getStyle().set("font-weight", "500");
        errorSpan.getStyle().set("margin-top", "var(--lumo-space-s)");
        errorSpan.setVisible(false);

        // Sıralama: Başlık -> Login Formu -> Hata Paneli (Formun hemen altında)
        add(new H1("Jollify Game Analytics"), login, errorSpan);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        var queryParameters = beforeEnterEvent.getLocation().getQueryParameters().getParameters();

        // Eğer hesap pasifse (?disabled ile geldiyse)
        if (queryParameters.containsKey("disabled")) {
            login.setError(true);
            errorSpan.setText("Hesabınız pasife alınmıştır.");
            errorSpan.setVisible(true);
        }
        // Eğer normal bir hata varsa (?error ile geldiyse - yanlış şifre/kullanıcı adı)
        else if (queryParameters.containsKey("error")) {
            login.setError(true);
            errorSpan.setText("Kullanıcı adı veya şifre hatalı.");
            errorSpan.setVisible(true);
        }
        // Hata yoksa gizle
        else {
            errorSpan.setVisible(false);
        }
    }
}