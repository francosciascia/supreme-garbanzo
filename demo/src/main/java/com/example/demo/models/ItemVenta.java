package com.example.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @Min(1)
    @Column(nullable = false)
    private int cantidad;

    @NotNull
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal precioUnitario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false) // FK -> productos.id
    private Producto producto;

    public ItemVenta(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecioVenta(); // congela precio actual
    }

    @Transient
    public BigDecimal getSubtotal() {
        BigDecimal pu = (precioUnitario != null) ? precioUnitario : BigDecimal.ZERO;
        return pu.multiply(BigDecimal.valueOf(cantidad));
    }
}