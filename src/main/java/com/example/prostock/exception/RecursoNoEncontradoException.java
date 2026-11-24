package com.example.prostock.exception;

/** Error de negocio para indicar que no existe el recurso solicitado. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}