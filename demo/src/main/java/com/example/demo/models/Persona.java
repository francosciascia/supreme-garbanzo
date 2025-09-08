package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nombre;
    private String apellido;
    private int edad;
    private int dni;
    private String direccion;

    private LocalDate inicio;

    @PrePersist
    public void prePersist() {
        if (inicio == null) inicio = LocalDate.now();
    }
}
