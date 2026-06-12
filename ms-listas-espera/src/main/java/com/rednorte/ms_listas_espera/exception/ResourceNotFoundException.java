package com.rednorte.ms_listas_espera.exception;

/**
 * Excepción personalizada para representar recursos no encontrados (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
