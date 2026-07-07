package com.dispatchflow.guides.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
<<<<<<< HEAD
import org.springframework.context.annotation.Profile;
=======
>>>>>>> origin/dev
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
<<<<<<< HEAD
@Profile("prod")
=======
>>>>>>> origin/dev
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API Stateless
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Actuator / Healthcheck público (para el API Gateway / Load Balancer)
                .requestMatchers("/actuator/health").permitAll()
                
                // 2. Rol 1: Permitir SOLO usar el endpoint de Descargar guías 
                .requestMatchers(HttpMethod.GET, "/api/guides/*/download").hasAnyAuthority("ROLE_DESCARGA", "ROLE_ADMIN")
                
                // 3. Rol 2: Permitir el uso del resto de endpoints (Crear, Modificar, Eliminar, Consultar)
                .requestMatchers("/api/guides/**").hasAuthority("ROLE_ADMIN")
                
                // Cualquier otra petición debe estar autenticada
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

<<<<<<< HEAD
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
=======
    // Este conversor extrae el custom claim de Azure AD B2C y lo convierte en un Rol de Spring Security
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        // Aquí defines el nombre exacto del atributo que crearás en Azure (ej. extension_Rol) ELIMINAR COMENTARIO
        grantedAuthoritiesConverter.setAuthoritiesClaimName("extension_Rol"); 
>>>>>>> origin/dev
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
}