package com.example.demo.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "reglas_operativas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReglasOperativas {
    @Id
    private Long id;
    @Column(name = "caja_obligatoria", nullable = false)
    private boolean cajaObligatoria;
    @Column(name = "requerir_cliente_venta", nullable = false)
    private boolean requerirClienteVenta;
    @Column(name = "permitir_venta_sin_stock", nullable = false)
    private boolean permitirVentaSinStock;
    @Column(name = "descuento_maximo", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoMaximo;
    @Column(name = "permitir_precio_manual", nullable = false)
    private boolean permitirPrecioManual;
    @Column(name = "medio_pago_predeterminado", nullable = false)
    @Builder.Default
    private String medioPagoPredeterminado = "EFECTIVO";
    @Column(name = "fiado_habilitado", nullable = false)
    private boolean fiadoHabilitado;
    @Column(name = "limite_credito_predeterminado", nullable = false, precision = 14, scale = 2)
    private BigDecimal limiteCreditoPredeterminado;
    @Column(name = "anulacion_solo_dueno", nullable = false)
    private boolean anulacionSoloDueno;
    @Column(name = "devoluciones_habilitadas", nullable = false)
    @Builder.Default
    private boolean devolucionesHabilitadas = true;
    @Column(name = "dias_maximos_devolucion", nullable = false)
    @Builder.Default
    private int diasMaximosDevolucion = 30;
    @Column(name = "controlar_vencimientos", nullable = false)
    @Builder.Default
    private boolean controlarVencimientos = true;
    @Column(name = "bloquear_venta_vencidos", nullable = false)
    @Builder.Default
    private boolean bloquearVentaVencidos = true;
    @Column(name = "dias_alerta_vencimiento", nullable = false)
    private int diasAlertaVencimiento;
    @Column(name = "redondeo_efectivo", nullable = false)
    @Builder.Default
    private boolean redondeoEfectivo = false;
    @Column(name = "motivo_anulacion_obligatorio", nullable = false)
    @Builder.Default
    private boolean motivoAnulacionObligatorio = false;
    @Column(name = "margen_minimo_pct", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal margenMinimoPct = BigDecimal.ZERO;
    @Column(name = "imprimir_ticket_auto", nullable = false)
    @Builder.Default
    private boolean imprimirTicketAuto = true;
}
