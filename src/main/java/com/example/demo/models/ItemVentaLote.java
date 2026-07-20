package com.example.demo.models;
import jakarta.persistence.*;import lombok.*;
@Entity @Table(name="items_venta_lotes") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ItemVentaLote{
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="item_venta_id")private ItemVenta itemVenta;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)@JoinColumn(name="lote_id")private LoteProducto lote;
    @Column(nullable=false)private Integer cantidad;
}
