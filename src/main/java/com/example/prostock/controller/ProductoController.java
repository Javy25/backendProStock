package com.example.prostock.controller;

import com.example.prostock.model.Producto;
import com.example.prostock.service.ProductoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expone consultas y operaciones de mantenimiento sobre el catálogo de productos. */
@RestController
@RequestMapping({"/api/productos", "/api/v1/productos"})
public class ProductoController {

    private final ProductoService productoService;

    @Autowired
    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // 1. Obtener todos los productos (GET /api/productos)
    /** Lista todos los productos con sus datos de catálogo y precio ofertado. */
    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        List<Producto> productos = productoService.obtenerTodos();
        return ResponseEntity.ok(productos);
    }

    // Endpoint dinÃƒÂ¡mico para filtrar por cualquier nombre de categorÃƒÂ­a (GET /api/productos/categoria/{categoria})
    /** Busca productos por el nombre de su categoría. */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Producto>> obtenerPorCategoria(@PathVariable String categoria) {
        List<Producto> productos = productoService.obtenerPorCategoria(categoria);
        return ResponseEntity.ok(productos);
    }

    // Ofertas: productos con descuento (GET /api/productos/ofertas)
    /** Lista los productos que tienen un descuento mayor que cero. */
    @GetMapping("/ofertas")
    public ResponseEntity<List<Producto>> obtenerOfertas() {
        return ResponseEntity.ok(productoService.obtenerOfertas());
    }

    // 2. Obtener un producto por ID (GET /api/productos/{id})
    /** Busca un producto por identificador y responde 404 cuando no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. Crear un nuevo producto (POST /api/productos)
    /** Crea un producto luego de validar descuento y resolver su categoría existente. */
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    public ResponseEntity<?> guardarProducto(@RequestBody Producto producto) {
        try {
            Producto nuevoProducto = productoService.guardarProducto(producto);
            return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Actualizar un producto existente (PUT /api/productos/{id})
    /** Actualiza un producto existente y responde con error si los datos no son válidos. */
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable Long id, @RequestBody Producto producto) {
        try {
            return productoService.actualizarProducto(id, producto)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Eliminar un producto (DELETE /api/productos/{id})
    /** Elimina un producto y responde 404 si el identificador no existe. */
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        if (productoService.eliminarProducto(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
