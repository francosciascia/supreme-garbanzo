package com.example.demo.services;

import com.example.demo.dto.AlertaOperativaDTO;
import com.example.demo.dto.ClienteDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.dto.ReglasOperativasDTO;
import com.example.demo.models.Caja;
import com.example.demo.repository.CajaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AlertasService {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));
    private static final BigDecimal UMBRAL_CREDITO = new BigDecimal("0.80");

    private final ProductoService productos;
    private final LoteService lotes;
    private final ClienteService clientes;
    private final CajaRepository cajas;
    private final ReglasOperativasService reglas;

    public AlertasService(ProductoService productos, LoteService lotes, ClienteService clientes,
                          CajaRepository cajas, ReglasOperativasService reglas) {
        this.productos = productos;
        this.lotes = lotes;
        this.clientes = clientes;
        this.cajas = cajas;
        this.reglas = reglas;
    }

    @Transactional(readOnly = true)
    public List<AlertaOperativaDTO> resumen() {
        ReglasOperativasDTO config = reglas.obtener();
        List<AlertaOperativaDTO> alertas = new ArrayList<>();

        List<ProductoDTO> stockBajo = productos.listar().stream()
                .filter(p -> p.stock() != null && p.stock() <= p.stockMinimo())
                .toList();
        if (!stockBajo.isEmpty()) {
            alertas.add(new AlertaOperativaDTO("STOCK", "ALTA",
                    stockBajo.size() + " producto(s) con stock bajo o en mínimo", "productos"));
        }

        if (config.controlarVencimientos()) {
            int dias = config.diasAlertaVencimiento();
            int vencen = lotes.proximosAVencer(dias).size();
            if (vencen > 0) {
                alertas.add(new AlertaOperativaDTO("VENCIMIENTOS", "MEDIA",
                        vencen + " lote(s) vencen en los próximos " + dias + " días", "lotes"));
            }
        }

        long cajasAbiertas = cajas.findByEstado(Caja.Estado.ABIERTA).size();
        if (cajasAbiertas > 0) {
            alertas.add(new AlertaOperativaDTO("CAJA", "BAJA",
                    cajasAbiertas + " caja(s) abierta(s)", "caja"));
        }

        if (config.fiadoHabilitado()) {
            BigDecimal limiteDefault = config.limiteCreditoPredeterminado();
            List<ClienteDTO> cerca = clientes.listar().stream()
                    .filter(c -> cercaDelLimite(c, limiteDefault))
                    .toList();
            if (!cerca.isEmpty()) {
                alertas.add(new AlertaOperativaDTO("CREDITO", "MEDIA",
                        cerca.size() + " cliente(s) cerca del límite de crédito (≥80%)", "clientes"));
            }
        }

        return alertas;
    }

    private boolean cercaDelLimite(ClienteDTO cliente, BigDecimal limiteDefault) {
        if (cliente.saldoCuenta() == null || cliente.saldoCuenta().signum() <= 0) return false;
        BigDecimal limite = cliente.limiteCredito() != null && cliente.limiteCredito().signum() > 0
                ? cliente.limiteCredito() : limiteDefault;
        if (limite == null || limite.signum() <= 0) return false;
        return cliente.saldoCuenta().divide(limite, 4, RoundingMode.HALF_UP).compareTo(UMBRAL_CREDITO) >= 0;
    }

    public static String formatearMonto(BigDecimal monto) {
        return MONEY.format(monto == null ? BigDecimal.ZERO : monto);
    }
}
