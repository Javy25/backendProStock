package com.example.prostock.exception;

/** Error de negocio usado cuando una operación entra en conflicto con datos existentes. */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}