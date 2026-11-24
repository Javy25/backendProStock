package com.example.prostock.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;

/** Verifica credenciales ID de Google contra la audiencia configurada por el servidor. */
@Service
public class GoogleAuthService {

    private final GoogleIdTokenVerifier verifier;

    /** Crea el verificador con el client ID esperado; sin él, la autenticación queda deshabilitada. */
    public GoogleAuthService(@Value("${google.client-id:}") String clientId) {
        verifier = clientId.isBlank()
                ? null
                : new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                        .setAudience(List.of(clientId))
                        .build();
    }

    /**
     * Comprueba firma, audiencia y correo verificado del ID token; no acepta datos de perfil
     * sin verificar y usa la parte local del correo como nombre alternativo.
     */
    public Optional<GoogleProfile> verifyCredential(String credential)
            throws GeneralSecurityException, IOException {
        if (verifier == null) {
            throw new GoogleAuthNotConfiguredException();
        }

        GoogleIdToken token;
        try {
            token = verifier.verify(credential);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
        if (token == null) {
            return Optional.empty();
        }

        GoogleIdToken.Payload payload = token.getPayload();
        if (!Boolean.TRUE.equals(payload.getEmailVerified())
                || payload.getEmail() == null
                || payload.getEmail().isBlank()) {
            return Optional.empty();
        }

        Object name = payload.get("name");
        String fullName = name instanceof String value && !value.isBlank()
                ? value
                : payload.getEmail().split("@", 2)[0];
        return Optional.of(new GoogleProfile(payload.getEmail(), fullName));
    }

    /** Datos mínimos del perfil que se entregan al servicio de clientes tras validar el token. */
    public record GoogleProfile(String email, String fullName) {
    }

    /** Señala que el servidor no tiene configurado el cliente OAuth de Google. */
    public static class GoogleAuthNotConfiguredException extends RuntimeException {
        public GoogleAuthNotConfiguredException() {
            super("Google authentication is not configured.");
        }
    }
}
