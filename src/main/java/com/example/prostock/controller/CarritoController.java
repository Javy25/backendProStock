package com.example.prostock.controller;

import com.example.prostock.DTO.AgregarCarritoRequest;
import com.example.prostock.model.Carrito;
import com.example.prostock.service.CarritoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Publica las operaciones del carrito para clientes registrados y para sesiones invitadas. */
@RestController
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // ==========================================
    // ENDPOINTS PARA CLIENTE AUTENTICADO
    // ==========================================

    /** Devuelve o inicializa el carrito persistente del cliente. */
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping({"/api/clientes/{clienteId}/carrito", "/api/v1/clientes/{clienteId}/carrito"})
    public ResponseEntity<Carrito> obtenerCarritoCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(clienteId));
    }

    /** Añade unidades de un producto al carrito persistente y valida errores de entrada. */
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping({"/api/clientes/{clienteId}/carrito/items", "/api/v1/clientes/{clienteId}/carrito/items"})
    public ResponseEntity<?> agregarProductoCliente(
            @PathVariable Long clienteId,
            @RequestBody AgregarCarritoRequest request) {
        try {
            Carrito carrito = carritoService.agregarProducto(
                    clienteId,
                    request.getProductoId(),
                    request.getCantidad()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Sustituye la cantidad de un producto dentro del carrito del cliente. */
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping({"/api/clientes/{clienteId}/carrito/items/{productoId}", "/api/v1/clientes/{clienteId}/carrito/items/{productoId}"})
    public ResponseEntity<?> actualizarCantidadCliente(
            @PathVariable Long clienteId,
            @PathVariable Long productoId,
            @RequestBody AgregarCarritoRequest request) {
        try {
            request.setProductoId(productoId);
            Carrito carrito = carritoService.actualizarCantidad(
                    clienteId,
                    request.getProductoId(),
                    request.getCantidad()
            );
            return ResponseEntity.ok(carrito);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Quita un producto concreto del carrito persistente. */
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping({"/api/clientes/{clienteId}/carrito/items/{productoId}", "/api/v1/clientes/{clienteId}/carrito/items/{productoId}"})
    public ResponseEntity<?> eliminarProductoCliente(
            @PathVariable Long clienteId,
            @PathVariable Long productoId) {
        try {
            Carrito carrito = carritoService.eliminarProducto(clienteId, productoId);
            return ResponseEntity.ok(carrito);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Elimina todos los artículos del carrito persistente del cliente. */
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping({"/api/clientes/{clienteId}/carrito", "/api/v1/clientes/{clienteId}/carrito"})
    public ResponseEntity<Carrito> vaciarCarritoCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.vaciarCarrito(clienteId));
    }

    // ==========================================
    // ENDPOINTS PARA INVITADO (SESIÃƒâ€œN)
    // ==========================================

    /** Devuelve o inicializa el carrito invitado guardado en la sesión HTTP. */
    @GetMapping({"/api/carrito", "/api/v1/carrito"})
    public ResponseEntity<Carrito> obtenerCarrito(HttpSession session) {
        return ResponseEntity.ok(carritoService.obtenerCarrito(session));
    }

    /** Añade un producto al carrito de la sesión invitada. */
    @PostMapping({"/api/carrito/items", "/api/v1/carrito/items"})
    public ResponseEntity<?> agregarProducto(@RequestBody AgregarCarritoRequest request, HttpSession session) {
        try {
            Carrito carrito = carritoService.agregarProducto(
                    session,
                    request.getProductoId(),
                    request.getCantidad()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(carrito);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Actualiza la cantidad de un producto en el carrito de la sesión. */
    @PutMapping({"/api/carrito/items/{productoId}", "/api/v1/carrito/items/{productoId}"})
    public ResponseEntity<?> actualizarCantidad(
            @PathVariable Long productoId,
            @RequestBody AgregarCarritoRequest request,
            HttpSession session) {
        try {
            request.setProductoId(productoId);
            Carrito carrito = carritoService.actualizarCantidad(
                    session,
                    request.getProductoId(),
                    request.getCantidad()
            );
            return ResponseEntity.ok(carrito);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Quita un producto concreto del carrito invitado. */
    @DeleteMapping({"/api/carrito/items/{productoId}", "/api/v1/carrito/items/{productoId}"})
    public ResponseEntity<?> eliminarProducto(
            @PathVariable Long productoId,
            HttpSession session) {
        try {
            Carrito carrito = carritoService.eliminarProducto(session, productoId);
            return ResponseEntity.ok(carrito);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Vacía el carrito invitado sin eliminar su sesión. */
    @DeleteMapping({"/api/carrito", "/api/v1/carrito"})
    public ResponseEntity<Carrito> vaciarCarrito(HttpSession session) {
        return ResponseEntity.ok(carritoService.vaciarCarrito(session));
    }
}
