package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "items_compra")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemCompra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "compra_id") private Compra compra;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "producto_id") private Producto producto;
    @Column(nullable = false) private Integer cantidad;
    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 2) private BigDecimal costoUnitario;
    @Column(name = "fecha_vencimiento") private LocalDate fechaVencimiento;
    @Transient public BigDecimal getSubtotal() { return costoUnitario.multiply(BigDecimal.valueOf(cantidad)); }
}
