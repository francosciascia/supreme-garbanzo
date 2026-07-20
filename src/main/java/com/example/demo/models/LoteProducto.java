package com.example.demo.models;
import jakarta.persistence.*;import lombok.*;import java.math.BigDecimal;import java.time.LocalDate;
@Entity @Table(name="lotes_producto") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoteProducto{
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="producto_id")private Producto producto;
    @Column(name="codigo_lote")private String codigoLote;
    @Column(name="fecha_ingreso",nullable=false)private LocalDate fechaIngreso;
    @Column(name="fecha_vencimiento")private LocalDate fechaVencimiento;
    @Column(name="cantidad_inicial",nullable=false)private Integer cantidadInicial;
    @Column(name="cantidad_disponible",nullable=false)private Integer cantidadDisponible;
    @Column(name="costo_unitario",nullable=false,precision=14,scale=2)private BigDecimal costoUnitario;
    @ManyToOne(fetch=FetchType.LAZY)@JoinColumn(name="compra_id")private Compra compra;
    @Column(nullable=false)@Builder.Default private boolean activo=true;
}
