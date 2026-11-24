package com.example.prostock.controller;

import com.example.prostock.DTO.MensajeContactoRequest;
import com.example.prostock.model.MensajeContacto;
import com.example.prostock.service.MensajeContactoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expone la bandeja de mensajes enviados desde el formulario de contacto. */
@RestController
@RequestMapping({"/api/mensajes", "/api/v1/mensajes"})
public class MensajeContactoController {

    private final MensajeContactoService service;

    public MensajeContactoController(MensajeContactoService service) {
        this.service = service;
    }

    /** Lista los mensajes de contacto del más reciente al más antiguo. */
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping
    public ResponseEntity<List<MensajeContacto>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    /** Valida y guarda un nuevo mensaje de contacto. */
    @PostMapping
    public ResponseEntity<MensajeContacto> crear(@RequestBody MensajeContactoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    /** Alterna el estado de atención del mensaje indicado. */
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}/atender")
    public ResponseEntity<MensajeContacto> alternarAtendido(@PathVariable Long id) {
        return ResponseEntity.ok(service.alternarAtendido(id));
    }

    /** Elimina un mensaje de la bandeja de contacto. */
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
