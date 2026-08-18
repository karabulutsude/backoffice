package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.User;
import com.jollifiy.backoffice.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
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
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@PageTitle("Kullanıcı Yönetimi")
@Route(value = "users", layout = MainLayout.class)
@PermitAll
public class UserView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class);

    public UserView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);

        add(new H2("Kullanıcı Yönetimi"));

        Button addButton = new Button("Yeni Kullanıcı Ekle", VaadinIcon.PLUS.create(), e -> openAddDialog());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> loadData());

        HorizontalLayout toolbar = new HorizontalLayout(addButton, refreshButton);
        toolbar.setWidthFull();

        grid.removeAllColumns();
        grid.addColumn(User::getId).setHeader("ID").setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Kullanıcı Adı").setSortable(true);
        grid.addColumn(User::getRole).setHeader("Rol").setSortable(true);

        // Aktif / Pasif Durum Kolonu (Görsel Badge ile)
        grid.addComponentColumn(user -> {
            boolean isActive = user.getIsActive() != null ? user.getIsActive() : true;
            Span badge = new Span(isActive ? "Aktif" : "Pasif");
            badge.getElement().getThemeList().add(isActive ? "badge success" : "badge error");
            return badge;
        }).setHeader("Durum").setSortable(true);

        // Özel Yetkiler Kolonu
        grid.addComponentColumn(user -> {
            VerticalLayout permsLayout = new VerticalLayout();
            permsLayout.setPadding(false);
            permsLayout.setSpacing(false);
            permsLayout.setAlignItems(FlexComponent.Alignment.START);

            if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
                String[] perms = user.getPermissions().split(",");
                for (String perm : perms) {
                    String trimmed = perm.trim();
                    String displayPerm = translatePermission(trimmed);

                    Span permSpan = new Span(displayPerm);
                    permSpan.getStyle().set("font-size", "var(--lumo-font-size-s)");
                    permsLayout.add(permSpan);
                }
            } else {
                permsLayout.add(new Span("-"));
            }
            return permsLayout;
        }).setHeader("Özel Yetkiler");

        // İşlemler Kolonu
        grid.addComponentColumn(user -> {
            Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(user));
            editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);

            Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteUser(user));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

            HorizontalLayout actionsLayout = new HorizontalLayout(editButton, deleteButton);
            actionsLayout.setSpacing(false);
            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private String translatePermission(String perm) {
        switch (perm.toUpperCase()) {
            case "CONFIG":
            case "KONFIGÜRASYON":
            case "KONFIGURASYON":
                return "Konfigürasyon";
            case "USERS":
            case "KULLANICILAR":
                return "Kullanıcılar";
            case "ANALYTICS":
            case "ANALİTİKLER":
            case "ANALITIKLER":
                return "Analitikler";
            case "DASHBOARD":
                return "Panel / Gösterge Paneli";
            case "OYUNCULAR":
                return "Oyuncular";
            case "İLERLEMELER":
            case "ILERLEMELER":
                return "İlerlemeler";
            default:
                return perm;
        }
    }

    private void openAddDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Yeni Kullanıcı Ekle");
        dialog.setWidth("450px");

        TextField usernameField = new TextField("Kullanıcı Adı");
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Şifre");
        passwordField.setWidthFull();

        TextField roleField = new TextField("Rol (Örn: ADMIN veya STAFF)");
        roleField.setValue("STAFF");
        roleField.setWidthFull();

        Checkbox activeCheckbox = new Checkbox("Aktif Et (Kullanıcı Sisteme Giriş Yapabilsin)");
        activeCheckbox.setValue(true);

        CheckboxGroup<String> permissionCheckboxes = new CheckboxGroup<>();
        permissionCheckboxes.setLabel("Erişebileceği Sayfalar");
        permissionCheckboxes.setItems("Oyuncular", "Analitikler", "İlerlemeler", "Konfigürasyon", "Kullanıcılar");
        permissionCheckboxes.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        Button saveButton = new Button("Kaydet", e -> {
            try {
                User user = new User();
                user.setUsername(usernameField.getValue());
                user.setPassword(passwordField.getValue());
                user.setRole(roleField.getValue());
                user.setIsActive(activeCheckbox.getValue());

                Set<String> selectedPerms = permissionCheckboxes.getValue();
                user.setPermissions(String.join(",", selectedPerms));

                userService.saveUser(user);

                Notification notif = Notification.show("Kullanıcı başarıyla eklendi!", 3000, Notification.Position.TOP_CENTER);
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

        VerticalLayout dialogLayout = new VerticalLayout(usernameField, passwordField, roleField, activeCheckbox, permissionCheckboxes, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void openEditDialog(User user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Kullanıcıyı Düzenle: " + user.getUsername());
        dialog.setWidth("450px");

        TextField usernameField = new TextField("Kullanıcı Adı");
        usernameField.setValue(user.getUsername() != null ? user.getUsername() : "");
        usernameField.setWidthFull();

        PasswordField passwordField = new PasswordField("Yeni Şifre (Boş bırakırsanız değişmez)");
        passwordField.setWidthFull();

        TextField roleField = new TextField("Rol");
        roleField.setValue(user.getRole() != null ? user.getRole() : "");
        roleField.setWidthFull();

        Checkbox activeCheckbox = new Checkbox("Aktif Et (Kullanıcı Sisteme Giriş Yapabilsin)");
        activeCheckbox.setValue(user.getIsActive() != null ? user.getIsActive() : true);

        CheckboxGroup<String> permissionCheckboxes = new CheckboxGroup<>();
        permissionCheckboxes.setLabel("Erişebileceği Modüller / Sayfalar");
        permissionCheckboxes.setItems("Oyuncular", "Analitikler", "İlerlemeler", "Konfigürasyon", "Kullanıcılar");
        permissionCheckboxes.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);

        if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
            Set<String> existingPerms = new HashSet<>();
            for (String p : user.getPermissions().split(",")) {
                existingPerms.add(translatePermission(p.trim()));
            }
            permissionCheckboxes.setValue(existingPerms);
        }

        final String existingPassword = user.getPassword();

        Button saveButton = new Button("Güncelle", e -> {
            try {
                user.setUsername(usernameField.getValue());

                String enteredPassword = passwordField.getValue();
                if (enteredPassword != null && !enteredPassword.trim().isEmpty()) {
                    user.setPassword(enteredPassword);
                } else {
                    user.setPassword(existingPassword);
                }

                user.setRole(roleField.getValue());
                user.setIsActive(activeCheckbox.getValue());

                Set<String> selectedPerms = permissionCheckboxes.getValue();
                user.setPermissions(String.join(",", selectedPerms));

                userService.saveUser(user);

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName().equals(user.getUsername())) {
                    List<GrantedAuthority> authorities = new ArrayList<>();

                    if (user.getRole() != null && !user.getRole().isEmpty()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
                    }

                    if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
                        for (String perm : user.getPermissions().split(",")) {
                            authorities.add(new SimpleGrantedAuthority(perm.trim().toUpperCase()));
                        }
                    }

                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            auth.getPrincipal(), auth.getCredentials(), authorities
                    );
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }

                Notification notif = Notification.show("Kullanıcı başarıyla güncellendi!", 3000, Notification.Position.TOP_CENTER);
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

        VerticalLayout dialogLayout = new VerticalLayout(usernameField, passwordField, roleField, activeCheckbox, permissionCheckboxes, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteUser(User user) {
        try {
            userService.deleteUser(user.getId());
            Notification notif = Notification.show("Kullanıcı silindi.", 3000, Notification.Position.TOP_CENTER);
            notif.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadData();
        } catch (Exception ex) {
            Notification.show("Silme hatası: " + ex.getMessage());
        }
    }

    private void loadData() {
        try {
            grid.setItems(userService.getAllUsers());
        } catch (Exception e) {
            Notification.show("Kullanıcılar yüklenirken hata: " + e.getMessage());
        }
    }
}