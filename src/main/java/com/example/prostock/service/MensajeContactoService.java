package com.example.prostock.service;

import com.example.prostock.DTO.MensajeContactoRequest;
import com.example.prostock.exception.RecursoNoEncontradoException;
import com.example.prostock.model.MensajeContacto;
import com.example.prostock.repository.MensajeContactoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Valida, registra y administra el estado de atención de los mensajes recibidos. */
@Service
public class MensajeContactoService {

    private final MensajeContactoRepository repository;

    public MensajeContactoService(MensajeContactoRepository repository) {
        this.repository = repository;
    }

    /** Devuelve los mensajes ordenados desde el más reciente. */
    public List<MensajeContacto> listar() {
        return repository.findAllByOrderByFechaDesc();
    }

    /** Normaliza y guarda los datos obligatorios del formulario como mensaje pendiente. */
    @Transactional
    public MensajeContacto crear(MensajeContactoRequest request) {
        if (request == null || request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        if (request.getAsunto() == null || request.getAsunto().isBlank()) {
            throw new IllegalArgumentException("El asunto es obligatorio.");
        }
        if (request.getMensaje() == null || request.getMensaje().isBlank()) {
            throw new IllegalArgumentException("El mensaje es obligatorio.");
        }

        MensajeContacto mensaje = new MensajeContacto();
        mensaje.setNombre(request.getNombre().trim());
        mensaje.setEmail(request.getEmail().trim().toLowerCase());
        mensaje.setAsunto(request.getAsunto().trim());
        mensaje.setMensaje(request.getMensaje().trim());
        mensaje.setFecha(LocalDateTime.now());
        mensaje.setAtendido(false);

        return repository.save(mensaje);
    }

    /** Cambia el mensaje entre pendiente y atendido. */
    @Transactional
    public MensajeContacto alternarAtendido(Long id) {
        MensajeContacto mensaje = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mensaje no encontrado con ID: " + id));
        mensaje.setAtendido(!Boolean.TRUE.equals(mensaje.getAtendido()));
        return repository.save(mensaje);
    }

    /** Elimina el mensaje solicitado o informa que no fue encontrado. */
    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Mensaje no encontrado con ID: " + id);
        }
        repository.deleteById(id);
    }
}
