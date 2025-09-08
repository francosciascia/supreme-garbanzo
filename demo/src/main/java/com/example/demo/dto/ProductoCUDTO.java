package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductoCUDTO(  // Lo que envía el usuario
            @NotBlank String nombre,
            String descripcion,
            @NotNull @Min(0) Integer stock,
            boolean vencimiento,
            @NotNull @PositiveOrZero BigDecimal costo,
            @NotNull @PositiveOrZero BigDecimal precioVenta
    ) {}
