package com.example.prostock.DTO;

import lombok.Data;

/** Datos que identifican el producto y las unidades que se agregan o ajustan en un carrito. */
@Data
public class AgregarCarritoRequest {
    private Long productoId;
    private Integer cantidad;
}
