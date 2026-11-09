package com.gabriel.helpdesk.config;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;


    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecretKey secretKey() {

        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }


    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {

        return NimbusJwtEncoder.withSecretKey(secretKey).build();
    }


    @Bean
    public JwtDecoder jwtDecoder(SecretKey secretKey) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("helpdesk-api"));

        return decoder;
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("perfil");
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return jwtConverter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // LOGIN
                        .requestMatchers("/auth/**").permitAll()
                        // USUÁRIOS: somente ADMIN
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        // ADMIN vê todos os chamados
                        .requestMatchers(HttpMethod.GET, "/chamados").hasRole("ADMIN")
                        // CLIENTE cria chamado
                        .requestMatchers(HttpMethod.POST, "/chamados").hasRole("CLIENTE")
                        // CLIENTE edita seu chamado
                        .requestMatchers(HttpMethod.PUT, "/chamados/*").hasRole("CLIENTE")
                        // SUPORTE visualiza disponíveis
                        .requestMatchers(HttpMethod.GET, "/chamados/disponiveis").hasRole("SUPORTE")
                        // ações do SUPORTE
                        .requestMatchers(HttpMethod.PATCH, "/chamados/*/analisar", "/chamados/*/assumir", "/chamados/*/aguardar-cliente", "/chamados/*/retomar", "/chamados/*/resolver").hasRole("SUPORTE")
                        // CLIENTE fecha
                        .requestMatchers(HttpMethod.PATCH, "/chamados/*/fechar").hasRole("CLIENTE")
                        // comentários
                        .requestMatchers(HttpMethod.POST, "/chamados/*/comentarios").hasAnyRole("CLIENTE", "SUPORTE")
                        // restante precisa apenas de JWT válido
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }
}