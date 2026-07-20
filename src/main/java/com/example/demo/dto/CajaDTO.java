package com.example.demo.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record CajaDTO(Long id, Long usuarioId, String usuario, LocalDateTime fechaApertura,
                      LocalDateTime fechaCierre, BigDecimal montoInicial, BigDecimal esperado,
                      BigDecimal real, BigDecimal diferencia, String estado) {}
