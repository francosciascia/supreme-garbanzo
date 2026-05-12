package com.example.demo.mapper;

import com.example.demo.dto.ClienteDTO;
import com.example.demo.models.Cliente;

public final class ClienteMapper {

    private ClienteMapper() {}

    public static ClienteDTO toDTO(Cliente c) {
        if (c == null) return null;
        return new ClienteDTO(
                c.getId(),
                c.getNombre(),
                c.getApellido(),
                c.getDni(),
                c.getEmail(),
                c.getTelefono(),
                c.getDireccion(),
                c.getFechaRegistro(),
                c.isActivo()
        );
    }
}
