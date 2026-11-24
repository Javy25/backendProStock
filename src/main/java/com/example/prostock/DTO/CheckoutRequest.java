package com.example.prostock.DTO;

import lombok.Data;

/** Datos que acompañan la confirmación de compra: medio de pago y dirección de entrega. */
@Data
public class CheckoutRequest {
    private String metodoPago;
    private String direccion;
}
