package com.example.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Min(0)
    @NotNull
    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private boolean vencimiento;

    @PositiveOrZero
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal costo;

    @PositiveOrZero
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioVenta;
}
