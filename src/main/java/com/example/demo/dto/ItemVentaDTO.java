package com.example.demo.dto;

import java.math.BigDecimal;

public record ItemVentaDTO(
        Long id,
        Long productoId,
        String nombreProducto,
        Integer cantidad,
        BigDecimal cantidadMostrada,
        String unidadVenta,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
