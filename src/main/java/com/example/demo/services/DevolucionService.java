package com.example.demo.services;

import com.example.demo.dto.DevolucionCreateDTO;
import com.example.demo.dto.DevolucionDTO;
import com.example.demo.dto.ReglasOperativasDTO;
import com.example.demo.models.Caja;
import com.example.demo.models.Devolucion;
import com.example.demo.models.ItemDevolucion;
import com.example.demo.models.ItemVenta;
import com.example.demo.models.ItemVentaLote;
import com.example.demo.models.LoteProducto;
import com.example.demo.models.MovimientoCaja;
import com.example.demo.models.MovimientoStock;
import com.example.demo.models.Producto;
import com.example.demo.models.Venta;
import com.example.demo.repository.CajaRepository;
import com.example.demo.repository.DevolucionRepository;
import com.example.demo.repository.MovimientoCajaRepository;
import com.example.demo.repository.MovimientoStockRepository;
import com.example.demo.repository.PersonaRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class DevolucionService {
    private final VentaRepository ventas;
    private final DevolucionRepository devoluciones;
    private final ProductoRepository productos;
    private final PersonaRepository personas;
    private final MovimientoStockRepository stock;
    private final CajaRepository cajas;
    private final MovimientoCajaRepository movimientosCaja;
    private final CuentaCorrienteService cuentas;
    private final AuditoriaService auditoria;
    private final ReglasOperativasService reglas;

    public DevolucionService(VentaRepository ventas, DevolucionRepository devoluciones, ProductoRepository productos,
                             PersonaRepository personas, MovimientoStockRepository stock, CajaRepository cajas,
                             MovimientoCajaRepository movimientosCaja, CuentaCorrienteService cuentas,
                             AuditoriaService auditoria, ReglasOperativasService reglas) {
        this.ventas = ventas;
        this.devoluciones = devoluciones;
        this.productos = productos;
        this.personas = personas;
        this.stock = stock;
        this.cajas = cajas;
        this.movimientosCaja = movimientosCaja;
        this.cuentas = cuentas;
        this.auditoria = auditoria;
        this.reglas = reglas;
    }

    @Transactional
    public DevolucionDTO crear(DevolucionCreateDTO d) {
        ReglasOperativasDTO config = reglas.obtener();
        if (!config.devolucionesHabilitadas()) {
            throw new IllegalStateException("Las devoluciones no están habilitadas");
        }
        Venta v = ventas.findById(d.ventaId()).orElseThrow(() -> new IllegalArgumentException("Venta inexistente"));
        if (v.getEstado() == Venta.Estado.ANULADA) {
            throw new IllegalStateException("No se puede devolver una venta anulada");
        }
        if (config.diasMaximosDevolucion() >= 0
                && v.getFecha().plusDays(config.diasMaximosDevolucion()).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("La venta superó el plazo máximo de devolución ("
                    + config.diasMaximosDevolucion() + " días)");
        }
        if (d.motivo() == null || d.motivo().isBlank()) {
            throw new IllegalArgumentException("Indicá el motivo");
        }
        Devolucion dev = Devolucion.builder()
                .venta(v)
                .fecha(LocalDateTime.now())
                .motivo(d.motivo().trim())
                .formaReintegro(Devolucion.FormaReintegro.valueOf(d.formaReintegro()))
                .usuario(d.usuarioId() == null ? null : personas.findById(d.usuarioId()).orElse(null))
                .build();
        for (var req : d.items()) {
            ItemVenta item = v.getItems().stream()
                    .filter(i -> Objects.equals(i.getId(), req.itemVentaId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Ítem ajeno a la venta"));
            int previo = devoluciones.findByVentaId(v.getId()).stream()
                    .flatMap(x -> x.getItems().stream())
                    .filter(x -> Objects.equals(x.getItemVenta().getId(), item.getId()))
                    .mapToInt(ItemDevolucion::getCantidad)
                    .sum();
            if (req.cantidad() <= 0 || previo + req.cantidad() > item.getCantidad()) {
                throw new IllegalArgumentException("Cantidad a devolver inválida para " + item.getProducto().getNombre());
            }
            Producto p = productos.findByIdForUpdate(item.getProducto().getId()).orElseThrow();
            int anterior = p.getStock();
            p.setStock(anterior + req.cantidad());
            restaurarLotes(item, previo, req.cantidad());
            BigDecimal factor = item.getUnidadVenta() == Producto.UnidadVenta.PESO
                    ? BigDecimal.valueOf(req.cantidad()).movePointLeft(3)
                    : BigDecimal.valueOf(req.cantidad());
            dev.agregar(ItemDevolucion.builder()
                    .itemVenta(item)
                    .cantidad(req.cantidad())
                    .subtotal(item.getPrecioUnitario().multiply(factor))
                    .build());
            stock.save(MovimientoStock.builder()
                    .producto(p)
                    .fecha(LocalDateTime.now())
                    .tipo(MovimientoStock.Tipo.ANULACION_VENTA)
                    .cantidad(req.cantidad())
                    .stockAnterior(anterior)
                    .stockNuevo(p.getStock())
                    .referencia("DEVOLUCION")
                    .descripcion(d.motivo())
                    .usuario(dev.getUsuario())
                    .build());
        }
        Devolucion saved = devoluciones.save(dev);
        if (saved.getFormaReintegro() == Devolucion.FormaReintegro.SALDO_A_FAVOR) {
            if (v.getCliente() == null) {
                throw new IllegalArgumentException("El saldo a favor requiere una venta con cliente");
            }
            cuentas.devolverCredito(v.getCliente(), saved.getTotal(), v, d.usuarioId());
        }
        if (saved.getFormaReintegro() == Devolucion.FormaReintegro.EFECTIVO) {
            if (d.usuarioId() == null) {
                throw new IllegalArgumentException("El reintegro en efectivo requiere un usuario");
            }
            Caja caja = cajas.findFirstByUsuarioIdAndEstadoOrderByFechaAperturaDesc(d.usuarioId(), Caja.Estado.ABIERTA)
                    .orElseThrow(() -> new IllegalStateException("Debés abrir una caja para reintegrar efectivo"));
            movimientosCaja.save(MovimientoCaja.builder()
                    .caja(caja)
                    .fecha(LocalDateTime.now())
                    .tipo(MovimientoCaja.Tipo.ANULACION)
                    .monto(saved.getTotal())
                    .descripcion("Devolución de " + v.getNumeroComprobante())
                    .build());
        }
        auditoria.registrar(d.usuarioId(), "CREAR", "DEVOLUCION", saved.getId(),
                "Venta " + v.getId() + " total " + saved.getTotal());
        return dto(saved);
    }

    @Transactional(readOnly = true)
    public List<DevolucionDTO> listar() {
        return devoluciones.findAll().stream().map(this::dto).toList();
    }

    private void restaurarLotes(ItemVenta item, int yaDevuelto, int cantidad) {
        int omitir = yaDevuelto;
        int restante = cantidad;
        for (ItemVentaLote asignacion : item.getLotes()) {
            if (omitir >= asignacion.getCantidad()) {
                omitir -= asignacion.getCantidad();
                continue;
            }
            int disponible = asignacion.getCantidad() - omitir;
            int devolver = Math.min(disponible, restante);
            LoteProducto lote = asignacion.getLote();
            lote.setCantidadDisponible(lote.getCantidadDisponible() + devolver);
            if (lote.getCantidadDisponible() > 0) {
                lote.setActivo(true);
            }
            restante -= devolver;
            omitir = 0;
            if (restante == 0) {
                break;
            }
        }
    }

    private DevolucionDTO dto(Devolucion d) {
        return new DevolucionDTO(
                d.getId(),
                d.getVenta().getId(),
                d.getFecha(),
                d.getMotivo(),
                d.getTotal(),
                d.getFormaReintegro().name(),
                d.getItems().stream()
                        .map(i -> new DevolucionDTO.ItemDTO(
                                i.getItemVenta().getId(),
                                i.getItemVenta().getProducto().getNombre(),
                                i.getCantidad(),
                                i.getSubtotal()))
                        .toList());
    }
}
