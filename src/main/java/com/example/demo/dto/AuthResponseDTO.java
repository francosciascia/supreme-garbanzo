package com.example.demo.dto;

import com.example.demo.models.Persona;

public record AuthResponseDTO(
    String token,
    String tipo,
    Long id,
    String email,
    String nombre,
    String apellido,
    String rol
) {
    public static AuthResponseDTO from(String token, Persona usuario) {
        return new AuthResponseDTO(
            token,
            "Bearer",
            usuario.getId(),
            usuario.getEmail(),
            usuario.getNombre(),
            usuario.getApellido(),
            usuario.getRol().toString()
        );
    }
}

