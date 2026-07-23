package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Compra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "proveedor_id") private Proveedor proveedor;
    @Column(nullable = false) private LocalDateTime fecha;
    @Column(name = "numero_comprobante") private String numeroComprobante;
    @Column(nullable = false, precision = 14, scale = 2) @Builder.Default private BigDecimal total = BigDecimal.ZERO;
    @Enumerated(EnumType.STRING) @Column(nullable = false) @Builder.Default private Estado estado = Estado.RECIBIDA;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "usuario_id") private Persona usuario;
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<ItemCompra> items = new ArrayList<>();

    public void agregarItem(ItemCompra item) {
        items.add(item);
        item.setCompra(this);
        total = total.add(item.getSubtotal());
    }
    public enum Estado { RECIBIDA, ANULADA }
}
