package com.example.prostock.repository;

import com.example.prostock.model.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repositorio para guardar mensajes de contacto y recuperar la bandeja por fecha. */
@Repository
public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {
    /** Lista los mensajes del más nuevo al más antiguo. */
    List<MensajeContacto> findAllByOrderByFechaDesc();
}
