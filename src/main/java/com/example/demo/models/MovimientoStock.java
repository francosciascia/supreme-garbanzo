package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_stock")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimientoStock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "producto_id") private Producto producto;
    @Column(nullable = false) private LocalDateTime fecha;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Tipo tipo;
    @Column(nullable = false) private Integer cantidad;
    @Column(name = "stock_anterior", nullable = false) private Integer stockAnterior;
    @Column(name = "stock_nuevo", nullable = false) private Integer stockNuevo;
    private String referencia;
    private String descripcion;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id") private Persona usuario;
    public enum Tipo { VENTA, COMPRA, AJUSTE, ANULACION_VENTA, ANULACION_COMPRA }
}
