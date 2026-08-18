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
        // Özel statik dosyalar ve dış kaynaklar için serbestlik tanıma
        http.authorizeHttpRequests(auth ->
                auth.requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll()
        );

        super.configure(http);

        // Vaadin standart login ekranını ve özel hata yönlendiricimizi bağlıyoruz
        setLoginView(http, LoginView.class);

        // Form login yapılandırmasına hata yönlendiricisini entegre ediyoruz
        http.formLogin(form -> form
                .failureHandler(authenticationFailureHandler())
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