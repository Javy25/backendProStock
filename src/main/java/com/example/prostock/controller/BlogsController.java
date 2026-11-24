package com.example.prostock.controller;

import com.example.prostock.model.Blogs;
import com.example.prostock.service.BlogsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Expone las operaciones HTTP para consultar y mantener las publicaciones del blog. */
@RestController
@RequestMapping({"/api/blogs", "/api/v1/blogs"})
public class BlogsController {

    private final BlogsService service;

    @Autowired
    public BlogsController(BlogsService service) {
        this.service = service;
    }

    /** Lista todas las publicaciones disponibles. */
    @GetMapping
    public List<Blogs> getAll() {
        return service.findAll();
    }

    /** Busca una publicación por su identificador y responde 404 si no existe. */
    @GetMapping("/{id}")
    public ResponseEntity<Blogs> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Crea una publicación con los datos recibidos en el cuerpo de la solicitud. */
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping
    public ResponseEntity<Blogs> create(@RequestBody Blogs blog) {
        Blogs saved = service.save(blog);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** Reemplaza los datos de una publicación existente. */
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<Blogs> update(@PathVariable Long id, @RequestBody Blogs blog) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Blogs updated = service.update(id, blog);
        return ResponseEntity.ok(updated);
    }

    /** Elimina una publicación existente y responde sin contenido. */
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (service.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
