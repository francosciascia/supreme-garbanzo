package com.example.demo.dto;

import java.time.LocalDate;

public record ClienteDTO(
        Long id,
        String nombre,
        String apellido,
        Integer dni,
        String email,
        String telefono,
        String direccion,
        LocalDate fechaRegistro,
        boolean activo
) {}
