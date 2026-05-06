package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "empleados")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class Empleado extends Persona {

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal salario;

    @Column(nullable = false)
    private String cargo;

    @Column(name = "fecha_contratacion")
    private LocalDate fechaContratacion;

    @Column(name = "horas_trabajo_semanal", nullable = false)
    private int horasTrabajoSemanal;

    @Column(nullable = false)
    private String departamento;

    @PrePersist
    public void prePersistEmpleado() {
        super.prePersist(); // Llama al prePersist de Persona
        if (fechaContratacion == null) {
            fechaContratacion = LocalDate.now();
        }
        if (horasTrabajoSemanal == 0) {
            horasTrabajoSemanal = 40; // Default 40 horas/semana
        }
    }
}
