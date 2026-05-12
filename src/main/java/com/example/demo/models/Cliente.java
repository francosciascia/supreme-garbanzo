package com.example.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

/**
 * Cliente del comercio (comprador). Es una entidad independiente de Persona,
 * porque el cliente no tiene login en el sistema. Las personas con login
 * (Empleado, Administrador) se modelan vía Persona.
 */
@Entity
@Table(
        name = "clientes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_clientes_dni", columnNames = "dni"),
                @UniqueConstraint(name = "uk_clientes_email", columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotBlank
    @Column(nullable = false)
    private String apellido;

    @NotNull
    @Column(nullable = false, unique = true)
    private Integer dni;

    @Email
    @Column(unique = true)
    private String email;

    private String telefono;

    private String direccion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDate fechaRegistro;

    @Column(nullable = false)
    @Builder.Default
    private boolean activo = true;

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDate.now();
        }
    }
}
