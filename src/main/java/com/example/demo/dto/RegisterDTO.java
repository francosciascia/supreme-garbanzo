package com.example.demo.dto;

import jakarta.validation.constraints.*;

public record RegisterDTO(
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    String email,

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
    String contraseña,

    @NotBlank(message = "El nombre es requerido")
    String nombre,

    @NotBlank(message = "El apellido es requerido")
    String apellido,

    @Min(value = 18, message = "Debes ser mayor de 18 años")
    @Max(value = 100, message = "La edad debe ser válida")
    int edad,

    @NotNull(message = "El DNI es requerido")
    int dni,

    String direccion
) {}

