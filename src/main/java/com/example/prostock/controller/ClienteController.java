package com.example.prostock.controller;

import com.example.prostock.DTO.LoginDTO;
import com.example.prostock.model.Cliente;
import com.example.prostock.service.ClienteService;
import com.example.prostock.service.GoogleAuthService;
import com.example.prostock.service.GoogleAuthService.GoogleAuthNotConfiguredException;
import com.example.prostock.security.JwtService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.security.GeneralSecurityException;

/** Agrupa las operaciones de clientes y los inicios de sesión local y con Google. */
@RestController
@RequestMapping({"/api/clientes", "/api/v1/clientes"})
public class ClienteController {

    private final ClienteService service;
    private final GoogleAuthService googleAuthService;
    private final JwtService jwtService;

    public ClienteController(
            ClienteService service,
            GoogleAuthService googleAuthService,
            JwtService jwtService) {
        this.service = service;
        this.googleAuthService = googleAuthService;
        this.jwtService = jwtService;
    }

    /** Lista los clientes registrados. */
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping
    public List<Cliente> all() { return service.findAll(); }

    /** Busca un cliente por identificador y responde 404 cuando no existe. */
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> get(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Registra un cliente a partir de los datos enviados. */
    @PostMapping
    public ResponseEntity<Cliente> create(@RequestBody Cliente c) {
        Cliente saved = service.save(c);
        return ResponseEntity.ok(saved);
    }

    /** Actualiza el perfil y fija el identificador de ruta antes de persistirlo. */
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> update(@PathVariable Long id, @RequestBody Cliente c) {
        if (!service.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        c.setId(id);
        Cliente updated = service.update(id, c);
        return ResponseEntity.ok(updated);
    }

    /** Elimina el cliente indicado si existe. */
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!service.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    /** Comprueba las credenciales locales y responde 401 si no coinciden. */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        return service.login(loginDTO.getEmail(), loginDTO.getContrasena())
                .<ResponseEntity<?>>map(cliente -> respuestaAutenticada(cliente, false))
                .orElseGet(() -> ResponseEntity.status(401).body("Credenciales incorrectas o usuario no encontrado"));
    }

    /** Verifica la credencial ID de Google y traduce fallos de configuración/verificación a HTTP. */
    @PostMapping("/login/google")
    public ResponseEntity<?> loginGoogle(@RequestBody GoogleLoginDTO loginDTO) {
        if (loginDTO.credential() == null || loginDTO.credential().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Falta la credencial de Google."));
        }

        try {
            return googleAuthService.verifyCredential(loginDTO.credential())
                    .<ResponseEntity<?>>map(profile -> respuestaAutenticada(
                            service.loginGoogle(profile.email(), profile.fullName()), true))
                    .orElseGet(() -> ResponseEntity.status(401)
                            .body(Map.of("error", "La credencial de Google no es válida.")));
        } catch (GoogleAuthNotConfiguredException e) {
            return ResponseEntity.status(503).body(Map.of("error",
                    "El inicio de sesión con Google no está configurado en el servidor."));
        } catch (GeneralSecurityException | IOException e) {
            return ResponseEntity.status(503).body(Map.of("error",
                    "No fue posible verificar la credencial de Google."));
        }
    }

    private ResponseEntity<Cliente> respuestaAutenticada(Cliente cliente, boolean googleVerificado) {
        String token = jwtService.crearToken(cliente, googleVerificado);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Auth-Role", jwtService.obtenerRol(cliente, googleVerificado))
                .body(cliente);
    }

    /** Recibe el ID token que entrega el flujo de inicio de sesión de Google. */
    public record GoogleLoginDTO(String credential) {
    }
}
