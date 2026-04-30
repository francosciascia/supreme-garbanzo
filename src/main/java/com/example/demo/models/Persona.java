package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "personas")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "persona_seq")
    @SequenceGenerator(name = "persona_seq", sequenceName = "persona_sequence", allocationSize = 1)
    private long id;

    @Column(nullable = false)
    private String nombre;
    
    @Column(nullable = false)
    private String apellido;
    
    @Column(nullable = false)
    private int edad;
    
    @Column(nullable = false, unique = true)
    private int dni;
    
    private String direccion;

    @Column(name = "fecha_inicio")
    private LocalDate inicio;

    @PrePersist
    public void prePersist() {
        if (inicio == null) inicio = LocalDate.now();
    }
}
