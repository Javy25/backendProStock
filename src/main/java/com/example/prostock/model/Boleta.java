package com.example.prostock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de compra asociada a un cliente; conserva una copia de los artículos para que
 * el detalle y los precios históricos no dependan de cambios posteriores del catálogo.
 */
@Entity
@Table(name = "boleta")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muchas boletas pueden pertenecer al mismo cliente.
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Los artículos se guardan como valores en una tabla auxiliar, no como entidades producto.
    @ElementCollection
    @CollectionTable(name = "boleta_items_app", joinColumns = @JoinColumn(name = "boleta_id"))
    private List<ItemCarrito> items = new ArrayList<>();

    private Double subtotal;
    private Double impuestos;
    private Double total;

    private LocalDateTime fecha;
    private String metodoPago;
    private String direccion;
    private String estado; // CREADA, PAGADA, ENVIADA

    /** Recalcula subtotal y total a partir de las líneas y los impuestos almacenados. */
    public void calcularTotal() {
        this.subtotal = items == null ? 0.0 : items.stream().mapToDouble(ItemCarrito::getSubtotal).sum();
        this.total = this.subtotal + (this.impuestos != null ? this.impuestos : 0.0);
    }
}
