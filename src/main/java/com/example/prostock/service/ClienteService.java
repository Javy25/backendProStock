package com.example.prostock.service;

import com.example.prostock.model.Cliente;
import com.example.prostock.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Implementa las consultas, cambios de perfil y autenticación de clientes. */
@Service
public class ClienteService {

    private final ClienteRepository repo;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    /** Lista todos los clientes registrados. */
    public List<Cliente> findAll() { return repo.findAll(); }

    /** Busca un cliente por su clave primaria. */
    public Optional<Cliente> findById(Long id) { return repo.findById(id); }

    /** Guarda cuentas nuevas con la contraseña cifrada de forma irreversible mediante BCrypt. */
    public Cliente save(Cliente c) {
        if (c.getContrasena() != null && !c.getContrasena().isBlank()) {
            c.setContrasena(passwordEncoder.encode(c.getContrasena()));
        }
        return repo.save(c);
    }

    /** Actualiza un perfil y conserva la contraseña guardada si el formulario la omite. */
    public Cliente update(Long id, Cliente c) {
        // Ensure the entity id matches the path id before saving
        c.setId(id);
        if (c.getContrasena() == null || c.getContrasena().isBlank()) {
            repo.findById(id).ifPresent(existente -> c.setContrasena(existente.getContrasena()));
        } else {
            c.setContrasena(passwordEncoder.encode(c.getContrasena()));
        }
        return repo.save(c);
    }

    /** Elimina un cliente por su identificador. */
    public void deleteById(Long id) { repo.deleteById(id); }

    // MÃ©todo para buscar al cliente por correo
    /** Busca la cuenta asociada al correo electrónico. */
    public Optional<Cliente> findByEmail(String email) {
        return repo.findByEmail(email);
    }

    /** Comprueba que exista el correo y que la contraseña recibida coincida. */
    public Optional<Cliente> login(String email, String contrasena) {
        Optional<Cliente> clienteOpt = repo.findByEmail(email);

        if (clienteOpt.isEmpty()) {
            return Optional.empty();
        }

        Cliente cliente = clienteOpt.get();
        String contrasenaGuardada = cliente.getContrasena();
        if (contrasenaGuardada == null) {
            return Optional.empty();
        }
        if (esHashBcrypt(contrasenaGuardada)) {
            return passwordEncoder.matches(contrasena, contrasenaGuardada)
                    ? Optional.of(cliente)
                    : Optional.empty();
        }
        if (!contrasenaGuardada.equals(contrasena)) {
            return Optional.empty();
        }

        // Migra contraseñas históricas en texto plano al primer inicio de sesión correcto.
        cliente.setContrasena(passwordEncoder.encode(contrasena));
        repo.save(cliente);
        return Optional.of(cliente);
    }

    private boolean esHashBcrypt(String contrasena) {
        return contrasena.startsWith("$2a$") || contrasena.startsWith("$2b$") || contrasena.startsWith("$2y$");
    }

    /** Reutiliza la cuenta del correo o crea un cliente para el perfil validado por Google. */
    public Cliente loginGoogle(String email, String nombreCompleto) {
        return repo.findByEmail(email).orElseGet(() -> {
            Cliente cliente = new Cliente();
            cliente.setEmail(email);
            cliente.setNombreCompleto(nombreCompleto);
            return repo.save(cliente);
        });
    }
}
