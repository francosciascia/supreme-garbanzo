package com.example.demo.dto;
public record ProveedorDTO(Long id, String nombre, String cuit, String telefono,
                           String email, String direccion, boolean activo) {}
