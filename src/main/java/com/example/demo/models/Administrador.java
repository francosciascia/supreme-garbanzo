package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "administradores")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Administrador extends Persona {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAcceso nivelAcceso;

    @Column(nullable = false)
    private String departamento;

    @Column(name = "permisos_especiales")
    private String permisosEspeciales;

    @Column(name = "fecha_nombramiento")
    private LocalDate fechaNombramiento;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private boolean activo = true;

    @PrePersist
    public void prePersistAdministrador() {
        super.prePersist(); // Llama al prePersist de Persona
        if (fechaNombramiento == null) {
            fechaNombramiento = LocalDate.now();
        }
        if (nivelAcceso == null) {
            nivelAcceso = NivelAcceso.BASICO;
        }
    }

    public enum NivelAcceso {
        BASICO,
        INTERMEDIO,
        AVANZADO,
        SUPER_ADMIN
    }
}
