package com.example.prostock.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Producto del catálogo, asociado a una categoría y con stock y descuento persistidos. */
@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "precio")
    private Integer precio;

    @Column(name = "nombre_modelo")
    private String nombreModelo;

    @Column(name = "descripcion", length = 600)
    private String descripcion;

    @Column(name = "link_imagen",length = 3000)
    private String linkImagen;

    // Varios productos pueden compartir una categoría; la FK reside en producto.
    @ManyToOne
    @JoinColumn(name = "tipo_categoria_id")
    private TipoCategoria tipoCategoria;

    @Column(name = "stock")
    private Integer stock;

    @Column(name = "descuento", nullable = false, columnDefinition = "integer default 0")
    private Integer descuento = 0;

    // Este valor calculado solo se serializa: el precio de oferta no es una columna editable.
    @JsonProperty(value = "precioOferta", access = JsonProperty.Access.READ_ONLY)
    @Transient
    /** Calcula el precio con descuento y redondea al entero más cercano. */
    public Integer getPrecioOferta() {
        if (precio == null) {
            return null;
        }
        int porcentaje = descuento == null ? 0 : descuento;
        if (porcentaje <= 0) {
            return precio;
        }
        return (int) Math.round(precio * (100 - porcentaje) / 100.0);
    }

    public Producto(Integer precio, String nombreModelo, String descripcion, String linkImagen, TipoCategoria tipoCategoria, Integer stock) {
        this.precio = precio;
        this.nombreModelo = nombreModelo;
        this.descripcion = descripcion;
        this.linkImagen = linkImagen;
        this.tipoCategoria = tipoCategoria;
        this.stock = stock;
    }
}
