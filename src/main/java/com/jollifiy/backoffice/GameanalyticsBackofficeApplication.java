package com.jollifiy.backoffice;

import com.jollifiy.backoffice.entity.User;
import com.jollifiy.backoffice.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class GameanalyticsBackofficeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameanalyticsBackofficeApplication.class, args);
    }

    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User admin = userRepository.findByUsername("admin").orElseGet(User::new);

            admin.setUsername("admin");
            // Şifreyi her zaman PasswordEncoder ile encode ederek kaydediyoruz
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRole("ADMIN");

            // Eğer yeni oluşturuluyorsa veya yetkileri null ise varsayılan yetkileri ver
            if (admin.getPermissions() == null || admin.getPermissions().isEmpty()) {
                admin.setPermissions("Konfigürasyon,Kullanıcılar,Analitikler");
            }

            userRepository.save(admin);
            System.out.println(">>> Admin kullanıcısının şifresi sıfırlandı ve doğrulandı: admin / 123456");
        };
    }
}