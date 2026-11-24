package com.example.prostock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA con los datos de perfil y acceso del cliente; la contraseña se acepta
 * al escribir el JSON, pero nunca se incluye en las respuestas serializadas.
 */
@Entity
@Table(name = "cliente")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombreCompleto", length = 150, nullable = false)
    @JsonProperty("nombreCompleto")
    private String nombreCompleto;

    @Column(name = "email", length = 100)
    private String email;

    // BCrypt necesita más espacio que una contraseña en texto plano; Hibernate amplía la columna.
    @Column(name = "contrasena", length = 100)
    // WRITE_ONLY permite recibirla en solicitudes sin exponerla en respuestas del API.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    @Column(name = "numero", length = 9)
    private String numero;
    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "region", length = 100)
    private String region;


    @Column(name = "comuna", length = 100)
    private String comuna;



    // Relación inversa: la clave foránea del carrito se mantiene en la entidad Carrito.
    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private com.example.prostock.model.Carrito carrito;
}
