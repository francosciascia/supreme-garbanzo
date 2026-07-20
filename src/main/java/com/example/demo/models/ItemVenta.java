package com.example.demo.models;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"venta", "producto"})
@Entity
@Table(name = "items_venta")
public class ItemVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // dueño de la relación Venta-Item (tiene la FK real)
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Min(1)
    @Column(nullable = false)
    private int cantidad;

    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    @Column(name = "costo_unitario", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal costoUnitario = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_venta", nullable = false)
    @Builder.Default
    private Producto.UnidadVenta unidadVenta = Producto.UnidadVenta.UNIDAD;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false) // FK -> productos.id
    private Producto producto;

    @OneToMany(mappedBy = "itemVenta", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ItemVentaLote> lotes = new ArrayList<>();

    public ItemVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta(); // congela precio actual
        this.costoUnitario = producto.getCosto();
        this.unidadVenta = producto.getUnidadVenta();
    }

    @Transient
    public BigDecimal getSubtotal() {
        BigDecimal pu = (precioUnitario != null) ? precioUnitario : BigDecimal.ZERO;
        BigDecimal factor = unidadVenta == Producto.UnidadVenta.PESO
                ? BigDecimal.valueOf(cantidad).divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(cantidad);
        return pu.multiply(factor);
    }

    public void asignarLote(LoteProducto lote, int cantidad) {
        ItemVentaLote asignacion = ItemVentaLote.builder().itemVenta(this).lote(lote).cantidad(cantidad).build();
        lotes.add(asignacion);
    }
}
