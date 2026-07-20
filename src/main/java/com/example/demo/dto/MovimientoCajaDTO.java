package com.example.demo.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record MovimientoCajaDTO(Long id, LocalDateTime fecha, String tipo, BigDecimal monto,
                                String descripcion, Long ventaId) {}
