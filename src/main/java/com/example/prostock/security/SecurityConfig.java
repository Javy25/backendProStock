package com.example.prostock.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Configura autorización sin estado, rutas públicas y operaciones privadas protegidas por JWT. */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            ClienteOwnershipFilter ownershipFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"error\":\"Se requiere un token Bearer válido.\"}");
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"error\":\"No tienes permiso para este recurso.\"}");
                        }))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST,
                                "/api/clientes", "/api/v1/clientes",
                                "/api/clientes/login", "/api/v1/clientes/login",
                                "/api/clientes/login/google", "/api/v1/clientes/login/google",
                                "/api/mensajes", "/api/v1/mensajes").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/productos/**", "/api/v1/productos/**",
                                "/api/categorias/**", "/api/v1/categorias/**",
                                "/api/blogs/**", "/api/v1/blogs/**").permitAll()
                        .requestMatchers("/api/carrito", "/api/carrito/**",
                                "/api/v1/carrito", "/api/v1/carrito/**").permitAll()
                        .requestMatchers("/api/clientes/*/carrito", "/api/clientes/*/carrito/**",
                                "/api/v1/clientes/*/carrito", "/api/v1/clientes/*/carrito/**",
                                "/api/clientes/*/boletas", "/api/clientes/*/boletas/**",
                                "/api/v1/clientes/*/boletas", "/api/v1/clientes/*/boletas/**")
                        .authenticated()
                        .requestMatchers("/api/boletas", "/api/boletas/**",
                                "/api/v1/boletas", "/api/v1/boletas/**")
                        .hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers(HttpMethod.GET, "/api/clientes", "/api/v1/clientes").hasRole("ADMIN")
                        .requestMatchers("/api/clientes/*", "/api/v1/clientes/*").authenticated()
                        .requestMatchers("/api/productos/**", "/api/v1/productos/**")
                        .hasAnyRole("ADMIN", "VENDEDOR")
                        .requestMatchers("/api/categorias/**", "/api/v1/categorias/**",
                                "/api/blogs/**", "/api/v1/blogs/**",
                                "/api/mensajes/**", "/api/v1/mensajes/**")
                        .hasRole("ADMIN")
                        .anyRequest().denyAll())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(ownershipFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /** Usa BCrypt para almacenar contraseñas nuevas y migrar las antiguas al iniciar sesión. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Evita que Spring Boot registre el filtro JWT dos veces fuera de la cadena de seguridad. */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterServletRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    /** Mantiene el filtro de propiedad exclusivamente dentro de la cadena de autorización. */
    @Bean
    public FilterRegistrationBean<ClienteOwnershipFilter> ownershipFilterServletRegistration(
            ClienteOwnershipFilter filter) {
        FilterRegistrationBean<ClienteOwnershipFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
