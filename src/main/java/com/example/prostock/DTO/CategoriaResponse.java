package com.example.prostock.DTO;

import com.example.prostock.model.TipoCategoria;

/** Representación pública de una categoría, sin exponer directamente su entidad JPA. */
public record CategoriaResponse(Long id, String nombre, String descripcion) {

    /** Convierte los campos de la entidad en el DTO de respuesta. */
    public static CategoriaResponse desde(TipoCategoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }
}