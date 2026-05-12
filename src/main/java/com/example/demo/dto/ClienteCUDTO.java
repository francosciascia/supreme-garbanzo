package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteCUDTO(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(min = 2, max = 60, message = "El apellido debe tener entre 2 y 60 caracteres")
        String apellido,

        @NotNull(message = "El DNI es obligatorio")
        Integer dni,

        @Email(message = "El email debe ser válido")
        String email,

        @Size(max = 30, message = "El teléfono no debe exceder 30 caracteres")
        String telefono,

        @Size(max = 200, message = "La dirección no debe exceder 200 caracteres")
        String direccion,

        Boolean activo
) 
{
        
}
