package com.example.demo.exceptions;

/**
 * Se lanza cuando las credenciales son inválidas o el token expiró.
 * Mapea a HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
