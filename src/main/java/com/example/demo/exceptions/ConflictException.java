package com.example.demo.exceptions;

/**
 * Se lanza cuando hay un conflicto con el estado del recurso
 * (por ejemplo, email/DNI ya registrado). Mapea a HTTP 409.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
