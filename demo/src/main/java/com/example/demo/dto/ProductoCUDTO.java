package com.example.demo.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductoCUDTO(

        @NotBlank
        String nombre,

        String descripcion,

        @NotNull
        @Min(0)
        Integer stock,

        @NotNull
        Boolean vencimiento,

        @NotNull
        @PositiveOrZero
        BigDecimal costo,

        @NotNull
        @PositiveOrZero
        BigDecimal precioVenta
) {}