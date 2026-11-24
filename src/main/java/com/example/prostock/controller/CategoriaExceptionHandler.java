package com.example.prostock.controller;

import com.example.prostock.exception.ConflictoException;
import com.example.prostock.exception.RecursoNoEncontradoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Convierte errores del catálogo en respuestas HTTP claras y acota el manejo a sus endpoints. */
@RestControllerAdvice(assignableTypes = CategoriaController.class)
public class CategoriaExceptionHandler {

    /** Informa solicitudes con datos inválidos como HTTP 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> datosInvalidos(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /** Traduce la ausencia de categoría a HTTP 404. */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<String> noEncontrada(RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    /** Informa conflictos de negocio, por ejemplo categorías aún asociadas a productos. */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<String> conflicto(ConflictoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    /** Traduce restricciones de unicidad de la base de datos a HTTP 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> integridad(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Ya existe una categoría con ese nombre");
    }
}