package com.example.demo.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record ProductoDTO(
        Long id,
        String nombre,
        String descripcion,
        Integer stock,
        Boolean vencimiento,
        BigDecimal costo,
        BigDecimal precioVenta,
        CategoriaDTO categoria,
        String codigoBarras,
        String marca,
        Integer stockMinimo,
        String unidadVenta,
        LocalDate fechaVencimiento,
        Integer cantidadMinimaPromo,
        BigDecimal precioPromocional,
        BigDecimal alicuotaIva
) {
    public ProductoDTO(
            Long id, String nombre, String descripcion, Integer stock, Boolean vencimiento,
            BigDecimal costo, BigDecimal precioVenta, CategoriaDTO categoria, String codigoBarras,
            String marca, Integer stockMinimo, String unidadVenta, LocalDate fechaVencimiento,
            Integer cantidadMinimaPromo, BigDecimal precioPromocional) {
        this(id, nombre, descripcion, stock, vencimiento, costo, precioVenta, categoria, codigoBarras,
                marca, stockMinimo, unidadVenta, fechaVencimiento, cantidadMinimaPromo, precioPromocional,
                new BigDecimal("21"));
    }

    public BigDecimal margenPct() {
        if (precioVenta == null || precioVenta.signum() == 0 || costo == null) return BigDecimal.ZERO;
        return precioVenta.subtract(costo).multiply(BigDecimal.valueOf(100))
                .divide(precioVenta, 1, RoundingMode.HALF_UP);
    }
}
