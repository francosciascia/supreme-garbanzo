package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record ItemVentaCreateDTO(

        @NotNull(message = "El ID del producto es obligatorio")
        Long productoId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor a 0")
        Integer cantidad,

        @DecimalMin(value = "0.01", message = "El precio manual debe ser mayor a cero")
        BigDecimal precioManual
) {
    public ItemVentaCreateDTO(Long productoId, Integer cantidad) {
        this(productoId, cantidad, null);
    }
}
