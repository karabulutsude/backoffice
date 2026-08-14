package com.jollifiy.backoffice.security;

import com.jollifiy.backoffice.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // 1. Eklendi
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(jsr250Enabled = true) // 2. Eklendi: @RolesAllowed anotasyonunu aktif eder!
public class SecurityConfig extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Özel statik dosyalar ve dış kaynaklar için serbestlik tanıma
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll()
        );

        super.configure(http);

        // Vaadin'in standart Login ekranını aktif ediyoruz
        setLoginView(http, LoginView.class);
    }
}
