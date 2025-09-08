package com.example.demo.dto;

import java.math.BigDecimal;

public record ItemVentaDTO(
        long productoId,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}