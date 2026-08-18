package com.jollifiy.backoffice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role; // Eski rol alanı (geriye dönük uyumluluk için kalabilir)

    @Column(length = 1000)
    private String permissions; // Örn: "CONFIG,USERS,ANALYTICS,DASHBOARD" (Seçilen yetkiler)

    @Column(nullable = false)
    private Boolean isActive = true; // Kullanıcının aktif/pasif durumu

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}