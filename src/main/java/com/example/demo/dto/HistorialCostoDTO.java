package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistorialCostoDTO(
        Long id,
        Long productoId,
        String producto,
        LocalDateTime fecha,
        BigDecimal costoAnterior,
        BigDecimal costoNuevo,
        String origen,
        String referencia
) {}
