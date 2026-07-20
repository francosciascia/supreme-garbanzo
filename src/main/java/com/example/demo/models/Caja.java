package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cajas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Caja {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "usuario_id") private Persona usuario;
    @Column(name = "fecha_apertura", nullable = false) private LocalDateTime fechaApertura;
    @Column(name = "fecha_cierre") private LocalDateTime fechaCierre;
    @Column(name = "monto_inicial", nullable = false, precision = 14, scale = 2) private BigDecimal montoInicial;
    @Column(name = "monto_final_esperado", precision = 14, scale = 2) private BigDecimal montoFinalEsperado;
    @Column(name = "monto_final_real", precision = 14, scale = 2) private BigDecimal montoFinalReal;
    @Column(precision = 14, scale = 2) private BigDecimal diferencia;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private Estado estado = Estado.ABIERTA;
    public enum Estado { ABIERTA, CERRADA }
}
