package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaDTO(
    Long id,
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    String nombre,
    String descripcion
) {}

