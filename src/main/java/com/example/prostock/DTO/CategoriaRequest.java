package com.example.prostock.DTO;

import lombok.Data;

/** Datos de entrada para crear o actualizar el nombre y la descripción de una categoría. */
@Data
public class CategoriaRequest {
    private String nombre;
    private String descripcion;
}