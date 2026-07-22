package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Presets de rubro: punto de partida de reglas. Después se pueden seguir editando a mano.
 */
public enum PresetRubro {
    KIOSCO(
            "Kiosco / maxikiosco",
            "Caja obligatoria, stock estricto, vencimientos activos y sin fiado.",
            reglas(true, false, false, "20", false, "EFECTIVO", false, "0", true, true, 15, true, true, 20)
    ),
    ALMACEN(
            "Almacén / despensa",
            "Reposición y vencimientos al frente; cliente opcional y descuentos moderados.",
            reglas(true, false, false, "15", false, "EFECTIVO", false, "0", true, true, 20, true, true, 30)
    ),
    FERRETERIA(
            "Ferretería / corralón",
            "Sin vencimientos, fiado habilitado y stock estricto.",
            reglas(true, false, false, "10", false, "EFECTIVO", true, "50000", true, true, 30, false, false, 0)
    ),
    REPUESTOS(
            "Repuestos (auto/moto)",
            "Fiado y precio manual; sin control de vencimientos.",
            reglas(true, false, false, "15", true, "EFECTIVO", true, "80000", true, true, 30, false, false, 0)
    ),
    VERDULERIA(
            "Verdulería / dietética",
            "Vencimientos activos, caja rápida y sin fiado.",
            reglas(true, false, false, "10", false, "EFECTIVO", false, "0", true, true, 7, true, true, 7)
    ),
    CARNICERIA(
            "Carnicería / fiambrería",
            "Vencimientos estrictos, bloquear vencidos y caja obligatoria.",
            reglas(true, false, false, "10", false, "EFECTIVO", false, "0", true, true, 7, true, true, 5)
    ),
    SERVICIO(
            "Servicio / taller",
            "Permite vender sin stock, cliente obligatorio, fiado y precio manual.",
            reglas(true, true, true, "25", true, "CUENTA_CORRIENTE", true, "100000", true, true, 30, false, false, 0)
    ),
    MOSTRADOR(
            "Mostrador simple",
            "Reglas mínimas: caja on, sin vencimientos ni fiado.",
            reglas(true, false, false, "10", false, "EFECTIVO", false, "0", true, true, 15, false, false, 0)
    );

    private final String nombre;
    private final String descripcion;
    private final ReglasOperativasDTO reglas;

    PresetRubro(String nombre, String descripcion, ReglasOperativasDTO reglas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.reglas = reglas;
    }

    public String nombre() {
        return nombre;
    }

    public String descripcion() {
        return descripcion;
    }

    public ReglasOperativasDTO reglas() {
        return reglas;
    }

    public String rubro() {
        return nombre;
    }

    public static List<PresetRubro> disponibles() {
        return Arrays.asList(values());
    }

    private static ReglasOperativasDTO reglas(
            boolean cajaObligatoria,
            boolean requerirCliente,
            boolean sinStock,
            String descuentoMax,
            boolean precioManual,
            String medioPago,
            boolean fiado,
            String limiteCredito,
            boolean anulacionSoloDueno,
            boolean devoluciones,
            int diasDevolucion,
            boolean controlarVencimientos,
            boolean bloquearVencidos,
            int diasAlerta) {
        return new ReglasOperativasDTO(
                cajaObligatoria,
                requerirCliente,
                sinStock,
                new BigDecimal(descuentoMax),
                precioManual,
                medioPago,
                fiado,
                new BigDecimal(limiteCredito),
                anulacionSoloDueno,
                devoluciones,
                diasDevolucion,
                controlarVencimientos,
                bloquearVencidos,
                diasAlerta);
    }
}
