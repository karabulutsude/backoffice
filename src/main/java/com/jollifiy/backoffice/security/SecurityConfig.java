package com.jollifiy.backoffice.security;

import com.jollifiy.backoffice.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig extends VaadinWebSecurity {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // ERİŞİM ENGELLENDİ SAYFASINI HERKESE AÇIYORUZ:
                .requestMatchers(new AntPathRequestMatcher("/access-denied")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/config/**")).hasAnyRole("CONFIG", "ADMIN", "KONFIGURASYON")
                .requestMatchers(new AntPathRequestMatcher("/analytics/**")).hasAnyRole("ANALYTICS", "ADMIN", "ANALITIK", "ANALİTİK")
                .requestMatchers(new AntPathRequestMatcher("/users/**")).hasAnyRole("USERS", "ADMIN", "KULLANICILAR")
        );

        super.configure(http);

        // Kilit ekranına yönlendirme
        http.exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/access-denied");
                })
        );

        // Vaadin standart login ekranı
        setLoginView(http, LoginView.class);

        http.formLogin(form -> form
                .failureHandler(authenticationFailureHandler())
        );

        http.logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request,
                                                HttpServletResponse response,
                                                AuthenticationException exception)
                    throws IOException, ServletException {

                String redirectUrl = "/login?error";

                // Eğer hata hesap pasifliğinden kaynaklanıyorsa URL'e ?disabled ekle
                if (exception instanceof DisabledException ||
                        (exception.getCause() != null && exception.getCause() instanceof DisabledException)) {
                    redirectUrl = "/login?disabled";
                }

                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            }
        };
    }
}