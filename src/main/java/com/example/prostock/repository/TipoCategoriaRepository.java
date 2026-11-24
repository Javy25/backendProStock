package com.example.prostock.repository;

import com.example.prostock.model.TipoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Repositorio de categorías con consultas por nombre y listado ordenado. */
@Repository
public interface TipoCategoriaRepository extends JpaRepository<TipoCategoria, Long> {

    /** Lista todas las categorías en orden alfabético por nombre. */
    List<TipoCategoria> findAllByOrderByNombreAsc();

    /** Busca una categoría sin distinguir mayúsculas del nombre. */
    Optional<TipoCategoria> findByNombreIgnoreCase(String nombre);
}