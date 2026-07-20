package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_caja")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MovimientoCaja {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "caja_id") private Caja caja;
    @Column(nullable = false) private LocalDateTime fecha;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Tipo tipo;
    @Column(nullable = false, precision = 14, scale = 2) private BigDecimal monto;
    private String descripcion;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "venta_id") private Venta venta;
    public enum Tipo { VENTA, INGRESO, RETIRO, ANULACION }
}
