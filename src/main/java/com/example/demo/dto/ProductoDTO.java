package com.example.demo.dto;

import java.math.BigDecimal;

public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        Integer stock,
        Boolean vencimiento,
        BigDecimal costo,
        BigDecimal precioVenta
) {}