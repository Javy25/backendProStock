package com.example.prostock.security;

import com.example.prostock.model.Cliente;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Firma y verifica los JWT propios de ProStock; los tokens duran doce horas. */
@Service
public class JwtService {

    private static final Duration DURACION_TOKEN = Duration.ofHours(12);

    private final SecretKey signingKey;
    private final Set<String> adminEmails;
    private final Set<String> sellerEmails;

    public JwtService(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.security.admin-emails:}") String adminEmails,
            @Value("${app.security.seller-emails:}") String sellerEmails) {
        if (secret.isBlank()) {
            throw new IllegalStateException(
                    "Falta APP_JWT_SECRET en el entorno o en secrets-local.properties.");
        }

        try {
            this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("APP_JWT_SECRET debe ser Base64 con al menos 32 bytes.", e);
        }
        this.adminEmails = normalizeEmails(adminEmails);
        this.sellerEmails = normalizeEmails(sellerEmails);
        if (this.adminEmails.stream().anyMatch(this.sellerEmails::contains)) {
            throw new IllegalStateException("Un correo no puede estar configurado como ADMIN y VENDEDOR.");
        }
    }

    private Set<String> normalizeEmails(String emails) {
        return Stream.of(emails.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** Crea un token; los roles privilegiados requieren identidad de Google verificada. */
    public String crearToken(Cliente cliente, boolean identidadGoogleVerificada) {
        Instant ahora = Instant.now();
        String rol = obtenerRol(cliente, identidadGoogleVerificada);

        return Jwts.builder()
                .subject(cliente.getId().toString())
                .claim("email", cliente.getEmail())
                .claim("role", rol)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(DURACION_TOKEN)))
                .signWith(signingKey)
                .compact();
    }

    /** Resuelve roles privilegiados desde las listas del servidor, solo tras verificar Google. */
    public String obtenerRol(Cliente cliente, boolean identidadGoogleVerificada) {
        if (!identidadGoogleVerificada) {
            return "CLIENTE";
        }
        String email = cliente.getEmail() == null ? "" : cliente.getEmail().trim().toLowerCase();
        if (adminEmails.contains(email)) {
            return "ADMIN";
        }
        if (sellerEmails.contains(email)) {
            return "VENDEDOR";
        }
        return "CLIENTE";
    }

    /** Verifica firma y vigencia antes de devolver la identidad del token. */
    public IdentidadToken verificarToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        IdentidadToken identidad = new IdentidadToken(
                Long.parseLong(claims.getSubject()),
                claims.get("email", String.class),
                claims.get("role", String.class));
        if (identidad.rol() == null || !Set.of("CLIENTE", "VENDEDOR", "ADMIN").contains(identidad.rol())) {
            throw new IllegalArgumentException("El rol del token no está reconocido.");
        }
        return identidad;
    }

    public record IdentidadToken(Long clienteId, String email, String rol) {
    }
}
