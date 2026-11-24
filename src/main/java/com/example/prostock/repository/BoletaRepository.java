package com.example.prostock.repository;

import com.example.prostock.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio para persistir boletas y buscar historiales por cliente o fecha. */
@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    /** Encuentra las boletas de un cliente mediante la relación cliente.id. */
    List<Boleta> findByClienteId(Long clienteId);

    /** Ordena todas las boletas desde la fecha más reciente. */
    List<Boleta> findAllByOrderByFechaDesc();
}
