package com.example.prostock.service;

import com.example.prostock.model.Blogs;
import com.example.prostock.repository.BlogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/** Centraliza el acceso y la persistencia de las publicaciones del blog. */
@Service
public class BlogsService {
    private final BlogsRepository repo;

    @Autowired
    public BlogsService(BlogsRepository repo) {
        this.repo = repo;
    }

    /** Devuelve todas las publicaciones guardadas. */
    public List<Blogs> findAll() {
        return repo.findAll();
    }

    /** Busca una publicación por su identificador. */
    public Optional<Blogs> findById(Long id) {
        return repo.findById(id);
    }

    /** Guarda una publicación nueva. */
    public Blogs save(Blogs blog) {
        return repo.save(blog);
    }

    /** Actualiza los campos de una publicación o asigna el identificador si no existía. */
    public Blogs update(Long id, Blogs blog) {
        return repo.findById(id).map(existing -> {
            existing.setNombre_post(blog.getNombre_post());
            existing.setTitulo(blog.getTitulo());
            existing.setDescripcion_post(blog.getDescripcion_post());
            existing.setContenido_post(blog.getContenido_post());
            existing.setContenido(blog.getContenido());
            existing.setLink_imagen_post(blog.getLink_imagen_post());
            return repo.save(existing);
        }).orElseGet(() -> {
            blog.setId_posteo(id);
            return repo.save(blog);
        });
    }

    /** Elimina la publicación indicada. */
    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
