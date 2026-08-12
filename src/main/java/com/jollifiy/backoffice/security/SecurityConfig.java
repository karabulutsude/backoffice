package com.jollifiy.backoffice.security;

import com.jollifiy.backoffice.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Vaadin'in güvenlik kurallarını çağırıyoruz
        super.configure(http);

        // Senin yazdığın LoginView'ı sistemin resmi giriş ekranı olarak tanıtıyoruz
        setLoginView(http, LoginView.class);
    }

    @Bean
    public UserDetailsService users() {
        // Panike giriş yapacak kullanıcı adı ve şifreyi burada belirliyoruz
        UserDetails admin = User.builder()
                .username("admin")
                .password("{noop}jollify123")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }
}