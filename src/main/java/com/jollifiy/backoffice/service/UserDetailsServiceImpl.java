package com.jollifiy.backoffice.service;

import com.jollifiy.backoffice.entity.User;
import com.jollifiy.backoffice.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // ADMIN ise her yetkiyi otomatik ekle
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            // Tüm sayfa yetkilerini veritabanındaki formatla eşleşecek şekilde ekle
            authorities.add(new SimpleGrantedAuthority("DASHBOARD"));
            authorities.add(new SimpleGrantedAuthority("PLAYERS"));
            authorities.add(new SimpleGrantedAuthority("ANALYTICS"));
            authorities.add(new SimpleGrantedAuthority("PROGRESS"));
            authorities.add(new SimpleGrantedAuthority("CONFIG"));
            authorities.add(new SimpleGrantedAuthority("USERS"));

        } else if (user.getPermissions() != null) {
            // Veritabanından gelen yetkileri virgülle ayırıp işliyoruz
            for (String perm : user.getPermissions().split(",")) {
                String cleanPerm = perm.trim().toUpperCase();
                if (!cleanPerm.isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(cleanPerm));

                    // Türkçe/Farklı yazımları standart İngilizce yetkilere eşle (Mapping)
                    if (cleanPerm.contains("ANALİTİK") || cleanPerm.contains("ANALYTICS")) {
                        authorities.add(new SimpleGrantedAuthority("ANALYTICS"));
                    }
                    if (cleanPerm.contains("KONFİGÜRASYON") || cleanPerm.contains("CONFIG")) {
                        authorities.add(new SimpleGrantedAuthority("CONFIG"));
                    }
                    if (cleanPerm.contains("OYUNCU") || cleanPerm.contains("PLAYERS")) {
                        authorities.add(new SimpleGrantedAuthority("PLAYERS"));
                    }
                    if (cleanPerm.contains("İLERLEME") || cleanPerm.contains("PROGRESS")) {
                        authorities.add(new SimpleGrantedAuthority("PROGRESS"));
                    }
                    if (cleanPerm.contains("KULLANICI") || cleanPerm.contains("USERS")) {
                        authorities.add(new SimpleGrantedAuthority("USERS"));
                    }
                }
            }
        }

        // Kullanıcının aktiflik durumu (Eğer null ise varsayılan olarak true kabul edilir)
        boolean enabled = user.getIsActive() != null ? user.getIsActive() : true;

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                enabled, // enabled (true: aktif, false: pasif / giriş yapamaz)
                true,    // accountNonExpired
                true,    // credentialsNonExpired
                true,    // accountNonLocked
                authorities
        );
    }
}