package com.example.prostock.repository;

import com.example.prostock.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio del catálogo con consultas derivadas por categoría, descuento y existencia. */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // MÃ©todo derivado para buscar productos por categorÃ­a exacta
    /** Busca productos por el nombre de la categoría, ignorando diferencias de mayúsculas. */
    List<Producto> findByTipoCategoria_NombreIgnoreCase(String nombre);

    /** Devuelve productos cuyo descuento supera el porcentaje indicado. */
    List<Producto> findByDescuentoGreaterThan(Integer descuento);

    /** Comprueba si hay productos vinculados a una categoría por su clave primaria. */
    boolean existsByTipoCategoria_Id(Long categoriaId);
}