package com.example.prostock.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Evita que un cliente acceda al perfil, carrito o boletas privadas de otra cuenta. */
@Component
public class ClienteOwnershipFilter extends OncePerRequestFilter {

    private static final Pattern RUTA_CLIENTE = Pattern.compile(
            "^/api/(?:v1/)?clientes/(\\d+)(?:/(?:carrito|boletas)(?:/.*)?)?$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Matcher ruta = RUTA_CLIENTE.matcher(request.getRequestURI());
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();

        if (ruta.matches() && autenticacion != null && autenticacion.isAuthenticated()) {
            boolean esAdministrador = autenticacion.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch("ROLE_ADMIN"::equals);
            if (!esAdministrador && !ruta.group(1).equals(autenticacion.getName())) {
                response.sendError(HttpStatus.FORBIDDEN.value(), "El recurso pertenece a otro cliente.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
