package com.example.prostock.repository;

import com.example.prostock.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Repositorio de acceso a clientes con búsqueda de cuenta por correo electrónico. */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    /** Resuelve una cuenta por su dirección de correo. */
    Optional<Cliente> findByEmail(String email);
}
