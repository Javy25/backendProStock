package com.example.prostock.DTO;
import lombok.Data;

/** Credenciales recibidas para iniciar sesión con correo y contraseña. */
@Data
public class LoginDTO {
    private String email;
    private String contrasena;
}