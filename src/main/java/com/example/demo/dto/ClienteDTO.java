package com.example.demo.dto;

import java.time.LocalDate;
import java.math.BigDecimal;

public record ClienteDTO(
        Long id,
        String nombre,
        String apellido,
        Integer dni,
        String email,
        String telefono,
        String direccion,
        LocalDate fechaRegistro,
        boolean activo,
        BigDecimal saldoCuenta,
        BigDecimal limiteCredito
) {}
