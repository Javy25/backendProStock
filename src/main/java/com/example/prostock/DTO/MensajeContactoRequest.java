package com.example.prostock.DTO;

import lombok.Data;

/** Campos que envía el formulario público de contacto para crear un mensaje. */
@Data
public class MensajeContactoRequest {
    private String nombre;
    private String email;
    private String asunto;
    private String mensaje;
}
