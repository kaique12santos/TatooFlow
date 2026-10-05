package com.tattooflow.config;

import com.tattooflow.common.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${spring.h2.console.enabled:false}")
    private boolean h2ConsoleEnabled;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(HttpMethod.POST, "/auth/login", "/api/auth/login").permitAll();
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/whatsapp/webhook").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMIN");
                if (h2ConsoleEnabled) {
                    auth.requestMatchers(PathRequest.toH2Console()).permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        if (h2ConsoleEnabled) {
            http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        }

        return http.build();
    }
}
