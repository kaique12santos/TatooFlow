package com.tattooflow.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tattooflow.common.response.ApiResponse;
import com.tattooflow.common.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import java.nio.charset.StandardCharsets;

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
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(HttpMethod.POST, "/auth/login", "/api/auth/login").permitAll();
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/whatsapp/webhook").permitAll();
                auth.requestMatchers(HttpMethod.POST, "/api/usuarios").hasRole("ADMIN");
                auth.requestMatchers(HttpMethod.POST, "/api/convites").hasRole("ADMIN");
<<<<<<< Updated upstream
=======
                auth.requestMatchers(HttpMethod.POST, "/api/convites/*/aceitar").permitAll();
                auth.requestMatchers(HttpMethod.GET, "/api/convites").hasRole("ADMIN");
>>>>>>> Stashed changes
                if (h2ConsoleEnabled) {
                    auth.requestMatchers(PathRequest.toH2Console()).permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .exceptionHandling(exceptions -> exceptions.accessDeniedHandler((request, response, exception) -> {
                if ("POST".equals(request.getMethod())
                        && (request.getContextPath() + "/api/convites").equals(request.getRequestURI())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    objectMapper.writeValue(response.getWriter(),
                            ApiResponse.error("Somente administradores podem enviar convites"));
                } else {
                    new AccessDeniedHandlerImpl().handle(request, response, exception);
                }
            }))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        if (h2ConsoleEnabled) {
            http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        }

        return http.build();
    }
}
