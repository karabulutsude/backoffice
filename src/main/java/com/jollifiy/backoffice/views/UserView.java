package com.jollifiy.backoffice.views;

import com.jollifiy.backoffice.entity.User;
import com.jollifiy.backoffice.service.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@PageTitle("Kullanıcı Yönetimi")
@Route(value = "users", layout = MainLayout.class)
@RolesAllowed({"ROLE_ADMIN", "ADMIN", "USERS", "KULLANICILAR", "Kullanıcılar", "Kullanicilar"})
public class UserView extends VerticalLayout {

    private final UserService userService;
    private final Grid<User> grid = new Grid<>(User.class);

    public UserView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);

        add(new H2("Kullanıcı Yönetimi"));

        Button refreshButton = new Button("Yenile", VaadinIcon.REFRESH.create(), e -> loadData());

        HorizontalLayout toolbar = new HorizontalLayout(refreshButton);
        toolbar.setWidthFull();

        // Yeni Kullanıcı Ekleme Yetkisi Kontrolü
        if (hasUserAddPermission()) {
            Button addButton = new Button("Yeni Kullanıcı Ekle", VaadinIcon.PLUS.create(), e -> openAddDialog());
            addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            toolbar.addComponentAtIndex(0, addButton);
        }

        grid.removeAllColumns();
        grid.addColumn(User::getId).setHeader("ID").setSortable(true);
        grid.addColumn(User::getUsername).setHeader("Kullanıcı Adı").setSortable(true);
        grid.addColumn(User::getRole).setHeader("Rol").setSortable(true);

        grid.addComponentColumn(user -> {
            boolean isActive = user.getIsActive() != null ? user.getIsActive() : true;
            Span badge = new Span(isActive ? "Aktif" : "Pasif");
            badge.getElement().getThemeList().add(isActive ? "badge success" : "badge error");
            return badge;
        }).setHeader("Durum").setSortable(true);

        // Özel Yetkiler Kolonu (Admin/Tam Yetki ve Özet Badge Görünümü)
        grid.addComponentColumn(user -> {
            Span badge = new Span();
            badge.getElement().getThemeList().add("badge");

            String perms = user.getPermissions();
            boolean isFullAdmin = "ADMIN".equalsIgnoreCase(user.getRole()) ||
                    (perms != null && perms.contains("CONFIG") && perms.contains("USERS") && perms.contains("İLERLEMELER") && perms.contains("ANALYTICS") && perms.contains("OYUNCULAR"));

            if (isFullAdmin || "admin".equalsIgnoreCase(user.getUsername())) {
                badge.setText("Tam Yetki (Full Access)");
                badge.getElement().getThemeList().add("success");
            } else if (perms != null && !perms.trim().isEmpty()) {
                String[] permArray = perms.split(",");
                int count = permArray.length;
                badge.setText(count + " Modül / İşlem Yetkisi");
                badge.getElement().getThemeList().add("contrast");
            } else {
                badge.setText("Yetki Yok");
                badge.getElement().getThemeList().add("error");
            }

            badge.getStyle().set("font-size", "var(--lumo-font-size-xs)");
            return badge;
        }).setHeader("Özel Yetkiler");

        boolean hasEditPermission = hasUserEditPermission();
        boolean hasDeletePermission = hasUserDeletePermission();

        grid.addComponentColumn(user -> {
            HorizontalLayout actionsLayout = new HorizontalLayout();
            actionsLayout.setSpacing(false);

            if (hasEditPermission) {
                Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(user));
                editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_TERTIARY);
                editButton.setTooltipText("Düzenle");
                actionsLayout.add(editButton);
            }

            if (hasDeletePermission) {
                Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteUser(user));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);
                deleteButton.setTooltipText("Sil");
                actionsLayout.add(deleteButton);
            }

            return actionsLayout;
        }).setHeader("İşlemler");

        grid.setSizeFull();

        add(toolbar, grid);
        loadData();
    }

    private boolean hasUserAddPermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        if (isAdmin(auth)) return true;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String u = a.getAuthority().toUpperCase();
            return u.equals("USER_ADD") || u.equals("KULLANICI_EKLE");
        });
    }

    private boolean hasUserEditPermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        if (isAdmin(auth)) return true;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String u = a.getAuthority().toUpperCase();
            return u.equals("USER_EDIT") || u.equals("KULLANICI_DUZENLE") || u.equals("KULLANICI_DÜZENLE");
        });
    }

    private boolean hasUserDeletePermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        if (isAdmin(auth)) return true;
        return auth.getAuthorities().stream().anyMatch(a -> {
            String u = a.getAuthority().toUpperCase();
            return u.equals("USER_DELETE") || u.equals("KULLANICI_SILME") || u.equals("KULLANICI_SİLME");
        });
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ADMIN") || a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    private String translatePermission(String perm) {
        switch (perm.toUpperCase()) {
            case "CONFIG": case "KONFIGÜRASYON": case "KONFIGURASYON": return "Konfigürasyon";
            case "CONFIG_ADD": return "Konfigürasyon Ekleme";
            case "CONFIG_EDIT": return "Konfigürasyon Düzenleme";
            case "CONFIG_DELETE": return "Konfigürasyon Silme";
            case "USERS": case "KULLANICILAR": return "Kullanıcılar";
            case "ANALYTICS": case "ANALİTİKLER": case "ANALITIKLER": return "Analitikler";
            case "ANALYTICS_DELETE": case "ANALITIK_SILME": case "ANALİTİK_SİLME": return "Analitik Silme";
            case "İLERLEMELER": case "ILERLEMELER": return "İlerlemeler";
            case "PROGRESS_EDIT": case "ILERLEME_DUZENLEME": case "İLERLEME_DÜZENLEME": return "İlerleme Düzenleme";
            case "PROGRESS_DELETE": case "ILERLEME_SILME": case "İLERLEME_SİLME": return "İlerleme Silme";
            case "DASHBOARD": return "Panel";
            case "OYUNCULAR": return "Oyuncular";
            case "PLAYER_EDIT": return "Oyuncu Düzenleme";
            case "PLAYER_BAN": return "Oyuncu Engelleme";
            case "PLAYER_DELETE": return "Oyuncu Silme";
            default: return perm;
        }
    }

    private void openAddDialog() {
        openDialog(null);
    }

    private void openEditDialog(User user) {
        openDialog(user);
    }

    private void openDialog(User editingUser) {
        boolean isEdit = (editingUser != null);
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isEdit ? "Kullanıcıyı Düzenle: " + editingUser.getUsername() : "Yeni Kullanıcı Ekle");
        dialog.setWidth("500px");

        TextField usernameField = new TextField("Kullanıcı Adı");
        usernameField.setWidthFull();
        if (isEdit) usernameField.setValue(editingUser.getUsername() != null ? editingUser.getUsername() : "");

        PasswordField passwordField = new PasswordField(isEdit ? "Yeni Şifre (Boş bırakırsanız değişmez)" : "Şifre");
        passwordField.setWidthFull();

        TextField roleField = new TextField("Rol");
        roleField.setValue(isEdit && editingUser.getRole() != null ? editingUser.getRole() : "STAFF");
        roleField.setWidthFull();

        Checkbox activeCheckbox = new Checkbox("Aktif Et (Kullanıcı Sisteme Giriş Yapabilsin)");
        activeCheckbox.setValue(isEdit ? (editingUser.getIsActive() != null ? editingUser.getIsActive() : true) : true);

        VerticalLayout permissionsContainer = new VerticalLayout();
        permissionsContainer.setPadding(false);
        permissionsContainer.setSpacing(true);
        permissionsContainer.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)");
        permissionsContainer.getStyle().set("padding", "12px");
        permissionsContainer.getStyle().set("border-radius", "var(--lumo-border-radius-m)");

        Span permTitle = new Span("Modül ve İşlem Yetkileri");
        permTitle.getStyle().set("font-weight", "bold");
        permTitle.getStyle().set("font-size", "var(--lumo-font-size-s)");

        // Konfigürasyon ve Alt Yetkileri
        Checkbox chkConfig = new Checkbox("Konfigürasyon (Ana Sayfa)");
        Checkbox chkConfigAdd = new Checkbox("└─ Konfigürasyon Ekleme");
        Checkbox chkConfigEdit = new Checkbox("└─ Konfigürasyon Düzenleme");
        Checkbox chkConfigDelete = new Checkbox("└─ Konfigürasyon Silme");
        chkConfigAdd.getStyle().set("margin-left", "20px");
        chkConfigEdit.getStyle().set("margin-left", "20px");
        chkConfigDelete.getStyle().set("margin-left", "20px");

        VerticalLayout subConfigLayout = new VerticalLayout(chkConfigAdd, chkConfigEdit, chkConfigDelete);
        subConfigLayout.setPadding(false);
        subConfigLayout.setSpacing(true);
        subConfigLayout.setVisible(false);

        chkConfig.addValueChangeListener(event -> {
            boolean isSelected = event.getValue();
            subConfigLayout.setVisible(isSelected);
            if (!isSelected) {
                chkConfigAdd.setValue(false);
                chkConfigEdit.setValue(false);
                chkConfigDelete.setValue(false);
            }
        });

        // Kullanıcılar Modülü
        Checkbox chkUsers = new Checkbox("Kullanıcılar");

        // İlerlemeler ve Alt Yetkileri
        Checkbox chkProgress = new Checkbox("İlerlemeler (Ana Sayfa)");
        Checkbox chkProgressEdit = new Checkbox("└─ İlerleme Düzenleme");
        Checkbox chkProgressDelete = new Checkbox("└─ İlerleme Silme");
        chkProgressEdit.getStyle().set("margin-left", "20px");
        chkProgressDelete.getStyle().set("margin-left", "20px");

        VerticalLayout subProgressLayout = new VerticalLayout(chkProgressEdit, chkProgressDelete);
        subProgressLayout.setPadding(false);
        subProgressLayout.setSpacing(true);
        subProgressLayout.setVisible(false);

        chkProgress.addValueChangeListener(event -> {
            boolean isSelected = event.getValue();
            subProgressLayout.setVisible(isSelected);
            if (!isSelected) {
                chkProgressEdit.setValue(false);
                chkProgressDelete.setValue(false);
            }
        });

        // Analitikler ve Alt Yetkisi
        Checkbox chkAnalytics = new Checkbox("Analitikler (Ana Sayfa)");
        Checkbox chkAnalyticsDelete = new Checkbox("└─ Analitik Silme");
        chkAnalyticsDelete.getStyle().set("margin-left", "20px");

        VerticalLayout subAnalyticsLayout = new VerticalLayout(chkAnalyticsDelete);
        subAnalyticsLayout.setPadding(false);
        subAnalyticsLayout.setSpacing(true);
        subAnalyticsLayout.setVisible(false);

        chkAnalytics.addValueChangeListener(event -> {
            boolean isSelected = event.getValue();
            subAnalyticsLayout.setVisible(isSelected);
            if (!isSelected) {
                chkAnalyticsDelete.setValue(false);
            }
        });

        // Oyuncular ve Alt Yetkileri
        Checkbox chkPlayers = new Checkbox("Oyuncular (Ana Sayfa)");
        Checkbox chkPlayerEdit = new Checkbox("└─ Oyuncu Düzenleme");
        Checkbox chkPlayerBan = new Checkbox("└─ Oyuncu Engelleme");
        Checkbox chkPlayerDelete = new Checkbox("└─ Oyuncu Silme");
        chkPlayerEdit.getStyle().set("margin-left", "20px");
        chkPlayerBan.getStyle().set("margin-left", "20px");
        chkPlayerDelete.getStyle().set("margin-left", "20px");

        VerticalLayout subPlayersLayout = new VerticalLayout(chkPlayerEdit, chkPlayerBan, chkPlayerDelete);
        subPlayersLayout.setPadding(false);
        subPlayersLayout.setSpacing(true);
        subPlayersLayout.setVisible(false);

        chkPlayers.addValueChangeListener(event -> {
            boolean isSelected = event.getValue();
            subPlayersLayout.setVisible(isSelected);
            if (!isSelected) {
                chkPlayerEdit.setValue(false);
                chkPlayerBan.setValue(false);
                chkPlayerDelete.setValue(false);
            }
        });

        permissionsContainer.add(permTitle, chkConfig, subConfigLayout, chkUsers, chkProgress, subProgressLayout, chkAnalytics, subAnalyticsLayout, chkPlayers, subPlayersLayout);

        if (isEdit && editingUser.getPermissions() != null) {
            String perms = editingUser.getPermissions();

            boolean hasConfig = perms.contains("CONFIG") || perms.contains("KONFIGÜRASYON") || perms.contains("KONFIGURASYON");
            chkConfig.setValue(hasConfig);
            subConfigLayout.setVisible(hasConfig);
            chkConfigAdd.setValue(perms.contains("CONFIG_ADD"));
            chkConfigEdit.setValue(perms.contains("CONFIG_EDIT"));
            chkConfigDelete.setValue(perms.contains("CONFIG_DELETE"));

            chkUsers.setValue(perms.contains("USERS") || perms.contains("KULLANICILAR"));

            boolean hasProgress = perms.contains("İLERLEMELER") || perms.contains("ILERLEMELER");
            chkProgress.setValue(hasProgress);
            subProgressLayout.setVisible(hasProgress);
            chkProgressEdit.setValue(perms.contains("PROGRESS_EDIT"));
            chkProgressDelete.setValue(perms.contains("PROGRESS_DELETE"));

            boolean hasAnalytics = perms.contains("ANALYTICS");
            chkAnalytics.setValue(hasAnalytics);
            subAnalyticsLayout.setVisible(hasAnalytics);
            chkAnalyticsDelete.setValue(perms.contains("ANALYTICS_DELETE"));

            boolean hasPlayers = perms.contains("OYUNCULAR");
            chkPlayers.setValue(hasPlayers);
            subPlayersLayout.setVisible(hasPlayers);
            chkPlayerEdit.setValue(perms.contains("PLAYER_EDIT"));
            chkPlayerBan.setValue(perms.contains("PLAYER_BAN"));
            chkPlayerDelete.setValue(perms.contains("PLAYER_DELETE"));
        }

        final String existingPassword = isEdit ? editingUser.getPassword() : "";

        Button saveButton = new Button(isEdit ? "Güncelle" : "Kaydet", e -> {
            try {
                User user = isEdit ? editingUser : new User();
                user.setUsername(usernameField.getValue());

                String enteredPassword = passwordField.getValue();
                if (enteredPassword != null && !enteredPassword.trim().isEmpty()) {
                    user.setPassword(enteredPassword);
                } else if (isEdit) {
                    user.setPassword(existingPassword);
                } else {
                    user.setPassword(enteredPassword);
                }

                user.setRole(roleField.getValue());
                user.setIsActive(activeCheckbox.getValue());

                List<String> selectedList = new ArrayList<>();

                if (chkConfig.getValue()) {
                    selectedList.add("CONFIG");
                    if (chkConfigAdd.getValue()) selectedList.add("CONFIG_ADD");
                    if (chkConfigEdit.getValue()) selectedList.add("CONFIG_EDIT");
                    if (chkConfigDelete.getValue()) selectedList.add("CONFIG_DELETE");
                }

                if (chkUsers.getValue()) selectedList.add("USERS");

                if (chkProgress.getValue()) {
                    selectedList.add("İLERLEMELER");
                    if (chkProgressEdit.getValue()) selectedList.add("PROGRESS_EDIT");
                    if (chkProgressDelete.getValue()) selectedList.add("PROGRESS_DELETE");
                }

                if (chkAnalytics.getValue()) {
                    selectedList.add("ANALYTICS");
                    if (chkAnalyticsDelete.getValue()) selectedList.add("ANALYTICS_DELETE");
                }

                if (chkPlayers.getValue()) {
                    selectedList.add("OYUNCULAR");
                    if (chkPlayerEdit.getValue()) selectedList.add("PLAYER_EDIT");
                    if (chkPlayerBan.getValue()) selectedList.add("PLAYER_BAN");
                    if (chkPlayerDelete.getValue()) selectedList.add("PLAYER_DELETE");
                }

                user.setPermissions(String.join(",", selectedList));

                userService.saveUser(user);

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName().equals(user.getUsername())) {
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    if (user.getRole() != null) authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase()));
                    for (String p : selectedList) {
                        authorities.add(new SimpleGrantedAuthority(p.toUpperCase()));
                    }
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities)
                    );
                }

                Notification.show(isEdit ? "Kullanıcı güncellendi!" : "Kullanıcı eklendi!", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                dialog.close();
                loadData();
            } catch (Exception ex) {
                Notification.show("Hata: " + ex.getMessage(), 3000, Notification.Position.TOP_CENTER);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("İptal", e -> dialog.close());
        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);

        VerticalLayout dialogLayout = new VerticalLayout(usernameField, passwordField, roleField, activeCheckbox, permissionsContainer, buttonsLayout);
        dialogLayout.setPadding(false);
        dialogLayout.setSpacing(true);

        dialog.add(dialogLayout);
        dialog.open();
    }

    private void deleteUser(User user) {
        try {
            userService.deleteUser(user.getId());
            Notification.show("Kullanıcı silindi.", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            loadData();
        } catch (Exception ex) {
            Notification.show("Silme hatası: " + ex.getMessage());
        }
    }

    private void loadData() {
        try {
            grid.setItems(userService.getAllUsers());
        } catch (Exception e) {
            Notification.show("Hata: " + e.getMessage());
        }
    }
}