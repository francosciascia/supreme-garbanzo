package com.example.demo.dto;

import java.math.BigDecimal;
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
        BigDecimal precioPromocional
) {}
