package com.example.demo.models;

import com.example.demo.exceptions.ValorMayorACeroException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "items")
@Entity
@Table(name="ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private LocalDate fecha;

    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<ItemVenta> items = new ArrayList<>();

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @PrePersist
    @PreUpdate
    public void calcularTotal(){
        if (fecha == null) fecha = LocalDate.now();
        if (total == null) total = BigDecimal.ZERO;
        total = items.stream()
                .map(ItemVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItems(ItemVenta itemVenta){
        items.add(itemVenta);
        itemVenta.setVenta(this);
        total = total.add(itemVenta.getSubtotal());
    }

    public void removeItem(ItemVenta itemVenta){
        if (items.remove(itemVenta)){
            itemVenta.setVenta(null);
            total = total.subtract(itemVenta.getSubtotal());
        }
    }
}