package com.example.demo.dto;

import java.math.BigDecimal;

public record ItemVentaDTO(
        Long id,
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}