package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductoDTO(  // Lo que se envía al usuario
            long id,
            String nombre,
            String descripcion,
            Integer stock,
            boolean vencimiento,
            BigDecimal precioVenta
    ) {}
