package com.example.prostock.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Línea de compra embebible en carrito y boleta, con una copia del nombre y precio unitario. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ItemCarrito {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private Double precioUnitario;

    /** Copia los valores de la línea para conservar una instantánea en la boleta. */
    public ItemCarrito copy() {
        return new ItemCarrito(this.id, this.productoId, this.nombreProducto, this.cantidad, this.precioUnitario);
    }

    /** Calcula el subtotal de la línea y devuelve cero si faltan precio o cantidad. */
    public Double getSubtotal() {
        return (precioUnitario != null && cantidad != null) ? precioUnitario * cantidad : 0.0;
    }
}