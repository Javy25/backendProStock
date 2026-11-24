package com.example.prostock.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA de una publicación. Mantiene columnas antiguas y actuales de título y contenido
 * para que los distintos consumidores del blog sigan siendo compatibles.
 */
@Entity
@Table(name = "blogs")
@Data
@NoArgsConstructor
public class Blogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_posteo;

    @Column(name = "titulo", length = 250, nullable = false)
    private String titulo;

    @Column(name = "nombre_post", length = 250, nullable = false)
    private String nombre_post;

    @Column(name = "descripcion_post", length = 300)
    private String descripcion_post;

    @Column(name = "contenido_post", length = 3000)
    private String contenido_post;

    @Column(name = "contenido", length = 3000, nullable = false)
    private String contenido;

    @Column(name = "link_imagen_post", length = 500)
    private String link_imagen_post;

    public Blogs(Long id_posteo, String nombre_post, String descripcion_post,
                 String contenido_post, String link_imagen_post) {
        this.id_posteo = id_posteo;
        this.nombre_post = nombre_post;
        this.titulo = nombre_post;
        this.descripcion_post = descripcion_post;
        this.contenido_post = contenido_post;
        this.contenido = contenido_post;
        this.link_imagen_post = link_imagen_post;
    }

    // Completa el alias que falte antes de insertar o actualizar para conservar ambas versiones.
    @PrePersist
    @PreUpdate
    private void sincronizarColumnasCompatibles() {
        if (titulo == null || titulo.isBlank()) {
            titulo = nombre_post;
        }
        if (nombre_post == null || nombre_post.isBlank()) {
            nombre_post = titulo;
        }
        if (contenido == null || contenido.isBlank()) {
            contenido = contenido_post;
        }
        if (contenido_post == null || contenido_post.isBlank()) {
            contenido_post = contenido;
        }
    }
}
