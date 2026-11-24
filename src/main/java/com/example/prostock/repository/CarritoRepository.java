package com.example.prostock.repository;

import com.example.prostock.model.Carrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Repositorio de carritos persistentes, con búsqueda por el cliente asociado. */
@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {
    /** Busca el único carrito persistente cuyo cliente tenga el identificador indicado. */
    Optional<Carrito> findByClienteId(Long clienteId);
}