package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public record CierreCajaResumenDTO(
        Long cajaId,
        BigDecimal montoInicial,
        BigDecimal totalVentas,
        BigDecimal totalIngresos,
        BigDecimal totalRetiros,
        BigDecimal totalAnulaciones,
        BigDecimal esperado,
        BigDecimal real,
        BigDecimal diferencia,
        List<MovimientoCajaDTO> movimientos
) {}
