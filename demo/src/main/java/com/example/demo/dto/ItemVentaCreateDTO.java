package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemVentaCreateDTO(
        @NotNull Long productoId,
        @Min(1) int cantidad
) {}