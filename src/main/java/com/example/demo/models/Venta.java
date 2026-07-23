package com.example.demo.models;

import com.example.demo.exceptions.ValorMayorACeroException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private LocalDateTime fecha;

    @OneToMany(
            mappedBy = "venta",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ItemVenta> items = new ArrayList<>();

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Estado estado = Estado.CONFIRMADA;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio_pago", nullable = false)
    @Builder.Default
    private MedioPago medioPago = MedioPago.EFECTIVO;

    @Column(name = "monto_recibido", precision = 14, scale = 2)
    private BigDecimal montoRecibido;

    @Column(nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal vuelto = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "monto_fiado", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal montoFiado = BigDecimal.ZERO;

    @Column(name = "numero_comprobante", unique = true, length = 40)
    private String numeroComprobante;

    @Column(name = "motivo_anulacion", length = 500)
    private String motivoAnulacion;

    /** Si se setea, @PrePersist/@PreUpdate respeta este total (p.ej. redondeo efectivo). */
    @Transient
    private BigDecimal totalForzado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id")
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Persona usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @PrePersist
    @PreUpdate
    public void calcularTotal(){
        if (fecha == null) fecha = LocalDateTime.now();
        if (total == null) total = BigDecimal.ZERO;
        BigDecimal bruto = items.stream()
                .map(ItemVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (descuento == null) descuento = BigDecimal.ZERO;
        total = bruto.subtract(descuento).max(BigDecimal.ZERO);
        if (totalForzado != null) total = totalForzado;
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

    public enum Estado { CONFIRMADA, ANULADA }
    public enum MedioPago { EFECTIVO, DEBITO, CREDITO, TRANSFERENCIA, CUENTA_CORRIENTE }
}
