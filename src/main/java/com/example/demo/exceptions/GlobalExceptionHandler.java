package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validaciones de @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                errors,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Argumentos inválidos / validaciones de negocio.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Conflictos de estado (sin stock, recursos duplicados, etc.).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {

        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Recurso no encontrado.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            WebRequest request) {

        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Autenticación fallida / token inválido.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex,
            WebRequest request) {

        return build(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    /**
     * Conflictos de negocio explícitos (email/DNI duplicado, etc.).
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException ex,
            WebRequest request) {

        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Fallback.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {

        ex.printStackTrace();

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getClass().getSimpleName() + ": " + ex.getMessage()
        );
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                message,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}
