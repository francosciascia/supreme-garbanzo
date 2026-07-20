package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "categoria")
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

    @Column(name = "codigo_barras", unique = true, length = 64)
    private String codigoBarras;

    private String marca;

    @NotNull
    @Column(nullable = false)
    private Integer stock;

    @Column(name = "stock_minimo", nullable = false)
    @Builder.Default
    private Integer stockMinimo = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_venta", nullable = false)
    @Builder.Default
    private UnidadVenta unidadVenta = UnidadVenta.UNIDAD;

    @Column(nullable = false)
    private boolean vencimiento;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @PositiveOrZero
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal costo;

    @PositiveOrZero
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "cantidad_minima_promo")
    private Integer cantidadMinimaPromo;

    @Column(name = "precio_promocional", precision = 14, scale = 2)
    private BigDecimal precioPromocional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    public enum UnidadVenta { UNIDAD, PESO }
}
