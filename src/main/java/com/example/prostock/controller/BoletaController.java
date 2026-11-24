package com.example.prostock.controller;

import com.example.prostock.DTO.CheckoutRequest;
import com.example.prostock.model.Boleta;
import com.example.prostock.service.BoletaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expone las operaciones de compra y consulta de boletas asociadas a un cliente. */
@RestController
@RequestMapping({"/api/clientes/{clienteId}/boletas", "/api/v1/clientes/{clienteId}/boletas"})
@SecurityRequirement(name = "BearerAuth")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    /** Confirma el carrito del cliente y crea su boleta de compra. */
    @PostMapping
    public ResponseEntity<?> crearBoleta(@PathVariable Long clienteId, @RequestBody CheckoutRequest request) {
        try {
            Boleta boleta = boletaService.crearDesdeCarrito(clienteId, request.getMetodoPago(), request.getDireccion());
            return ResponseEntity.status(HttpStatus.CREATED).body(boleta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Lista las boletas que pertenecen al cliente indicado. */
    @GetMapping
    public ResponseEntity<List<Boleta>> listarBoletas(@PathVariable Long clienteId) {
        return ResponseEntity.ok(boletaService.listarPorCliente(clienteId));
    }

    /** Obtiene una boleta solo si pertenece al cliente indicado. */
    @GetMapping("/{boletaId}")
    public ResponseEntity<?> obtenerBoleta(@PathVariable Long clienteId, @PathVariable Long boletaId) {
        try {
            Boleta b = boletaService.obtener(clienteId, boletaId);
            return ResponseEntity.ok(b);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
