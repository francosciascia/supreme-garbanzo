package com.example.demo.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoCUDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
        String descripcion,

        @NotNull(message = "El stock es obligatorio")
        @Min(value = 0, message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "Debe indicar si el producto vence")
        Boolean vencimiento,

        @NotNull(message = "El costo es obligatorio")
        @DecimalMin(value = "0.01", message = "El costo debe ser mayor a 0")
        BigDecimal costo,

        @NotNull(message = "El precio de venta es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio de venta debe ser mayor a 0")
        BigDecimal precioVenta
) {}