package com.example.demo.services;

import com.example.demo.dto.ItemVentaCreateDTO;
import com.example.demo.dto.VentaCreateDTO;
import com.example.demo.dto.VentaDTO;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.mapper.VentaMapper;
import com.example.demo.models.Cliente;
import com.example.demo.models.ItemVenta;
import com.example.demo.models.Producto;
import com.example.demo.models.Venta;
import com.example.demo.repository.ClienteRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.VentaRepository;
import com.example.demo.repository.CajaRepository;
import com.example.demo.repository.MovimientoCajaRepository;
import com.example.demo.repository.MovimientoStockRepository;
import com.example.demo.repository.PersonaRepository;
import com.example.demo.models.Caja;
import com.example.demo.models.MovimientoCaja;
import com.example.demo.models.MovimientoStock;
import com.example.demo.models.LoteProducto;
import com.example.demo.repository.LoteProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    private final ClienteRepository clienteRepository;
    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final PersonaRepository personaRepository;
    private final LoteProductoRepository loteProductoRepository;

    @Autowired(required = false)
    private ReglasOperativasService reglasOperativasService;

    @Autowired(required = false)
    private CuentaCorrienteService cuentaCorrienteService;

    @Autowired(required = false)
    private EmpleadoService empleadoService;

    @Autowired
    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                        ClienteRepository clienteRepository, CajaRepository cajaRepository,
                        MovimientoCajaRepository movimientoCajaRepository, MovimientoStockRepository movimientoStockRepository,
                        PersonaRepository personaRepository, LoteProductoRepository loteProductoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.cajaRepository = cajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.movimientoStockRepository = movimientoStockRepository;
        this.personaRepository = personaRepository;
        this.loteProductoRepository = loteProductoRepository;
    }

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                        ClienteRepository clienteRepository) {
        this(ventaRepository, productoRepository, clienteRepository, null, null, null, null, null);
    }

    // ===== Lectura =====
    @Transactional(readOnly = true)
    public List<Venta> listar() {
        return ventaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> listarDTO() {
        return ventaRepository.findAll()
                .stream()
                .map(VentaMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<Venta> detalle(Long id) {
        return ventaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<VentaDTO> detalleDTO(Long id) {
        return ventaRepository.findById(id).map(VentaMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<VentaDTO> buscar(Long clienteId, LocalDate desde, LocalDate hasta, Pageable pageable) {
        Specification<Venta> specification = (root, query, cb) -> cb.conjunction();
        if (clienteId != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId));
        }
        if (desde != null) {
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), desde.atStartOfDay()));
        }
        if (hasta != null) {
            specification = specification.and((root, query, cb) -> cb.lessThan(root.get("fecha"), hasta.plusDays(1).atStartOfDay()));
        }
        return ventaRepository.findAll(specification, pageable).map(VentaMapper::toDTO);
    }

    // ===== Crear venta con DTO =====
    @Transactional
    public Venta crear(VentaCreateDTO ventaDTO) {
        com.example.demo.dto.ReglasOperativasDTO reglas = reglasOperativasService == null ? null : reglasOperativasService.obtener();
        Venta venta = new Venta();
        venta.setFecha(java.time.LocalDateTime.now());
        venta.setTotal(java.math.BigDecimal.ZERO);
        venta.setEstado(Venta.Estado.CONFIRMADA);
        venta.setMedioPago(parseMedioPago(ventaDTO.medioPago()));
        venta.setDescuento(ventaDTO.descuento() == null ? BigDecimal.ZERO : ventaDTO.descuento());
        if (venta.getDescuento().signum() < 0) throw new IllegalArgumentException("El descuento no puede ser negativo");
        if (reglas != null && reglas.cajaObligatoria() && ventaDTO.cajaId() == null) throw new IllegalStateException("Debés abrir una caja antes de vender");
        if (reglas != null && reglas.requerirClienteVenta() && ventaDTO.clienteId() == null)
            throw new IllegalArgumentException("Esta venta requiere un cliente");
        if (venta.getMedioPago() == Venta.MedioPago.CUENTA_CORRIENTE && (reglas == null || !reglas.fiadoHabilitado()))
            throw new IllegalStateException("La cuenta corriente no está habilitada");
        if (ventaDTO.cajaId() != null) {
            if (cajaRepository == null) throw new IllegalStateException("Caja no disponible");
            Caja caja = cajaRepository.findById(ventaDTO.cajaId()).orElseThrow(() -> new IllegalArgumentException("Caja inexistente"));
            if (caja.getEstado() != Caja.Estado.ABIERTA) throw new IllegalStateException("La caja esta cerrada");
            venta.setCaja(caja);
        }
        if (ventaDTO.usuarioId() != null && personaRepository != null) {
            venta.setUsuario(personaRepository.findById(ventaDTO.usuarioId()).orElseThrow(() -> new IllegalArgumentException("Usuario inexistente")));
        }

        if (ventaDTO.clienteId() != null) {
            Cliente cliente = clienteRepository.findById(ventaDTO.clienteId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cliente no encontrado: " + ventaDTO.clienteId()));
            venta.setCliente(cliente);
        }

        for (ItemVentaCreateDTO itemDTO : ventaDTO.items()) {
            Long productoId = itemDTO.productoId();
            Producto p = productoRepository.findByIdForUpdate(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productoId));

            boolean permitirSinStock = reglas != null && reglas.permitirVentaSinStock();
            if (!permitirSinStock && (p.getStock() == null || p.getStock() < itemDTO.cantidad())) {
                throw new IllegalStateException("Sin stock suficiente para " + p.getNombre() +
                    ". Stock disponible: " + p.getStock());
            }
            if (!permitirSinStock) validarStockVendible(p, itemDTO.cantidad(), reglas);

            ItemVenta item = new ItemVenta();
            item.setProducto(p);
            item.setCantidad(itemDTO.cantidad());
            if (itemDTO.precioManual() != null) {
                if (reglas == null || !reglas.permitirPrecioManual())
                    throw new IllegalStateException("El precio manual no está habilitado");
                if (ventaDTO.usuarioId() != null && empleadoService != null
                        && !empleadoService.tiene(ventaDTO.usuarioId(), com.example.demo.models.PermisoUsuario.Permiso.MODIFICAR_PRECIOS))
                    throw new IllegalStateException("El usuario no tiene permiso para modificar precios");
                item.setPrecioUnitario(itemDTO.precioManual());
            } else {
                item.setPrecioUnitario(precioAplicable(p, itemDTO.cantidad()));
            }
            item.setCostoUnitario(p.getCosto());
            item.setUnidadVenta(p.getUnidadVenta());
            item.setVenta(venta);
            asignarLotes(item, p, itemDTO.cantidad(), reglas);

            p.setStock(p.getStock() - itemDTO.cantidad());
            venta.addItems(item);
            registrarStock(p, -itemDTO.cantidad(), p.getStock() + itemDTO.cantidad(), MovimientoStock.Tipo.VENTA, null, venta.getUsuario());
        }
        venta.calcularTotal();
        if (venta.getDescuento().compareTo(venta.getTotal().add(venta.getDescuento())) > 0)
            throw new IllegalArgumentException("El descuento no puede superar el subtotal");
        if (reglas != null) {
            BigDecimal bruto = venta.getTotal().add(venta.getDescuento());
            BigDecimal maximo = bruto.multiply(reglas.descuentoMaximo()).movePointLeft(2);
            if (venta.getDescuento().compareTo(maximo) > 0) throw new IllegalArgumentException("El descuento supera el máximo permitido");
        }
        if (venta.getMedioPago() == Venta.MedioPago.EFECTIVO) {
            BigDecimal recibido = ventaDTO.montoRecibido() == null ? venta.getTotal() : ventaDTO.montoRecibido();
            if (recibido.compareTo(venta.getTotal()) < 0) throw new IllegalArgumentException("El monto recibido no alcanza");
            venta.setMontoRecibido(recibido); venta.setVuelto(recibido.subtract(venta.getTotal()));
        } else { venta.setMontoRecibido(venta.getTotal()); venta.setVuelto(BigDecimal.ZERO); }
        Venta guardada = ventaRepository.save(venta);
        guardada.setNumeroComprobante("V-" + String.format("%08d", guardada.getId()));
        if (guardada.getMedioPago() == Venta.MedioPago.CUENTA_CORRIENTE) {
            if (cuentaCorrienteService == null) throw new IllegalStateException("Cuenta corriente no disponible");
            cuentaCorrienteService.registrarVenta(guardada, ventaDTO.usuarioId());
        }
        if (guardada.getCaja() != null && guardada.getMedioPago() == Venta.MedioPago.EFECTIVO && movimientoCajaRepository != null) {
            movimientoCajaRepository.save(MovimientoCaja.builder().caja(guardada.getCaja()).venta(guardada).fecha(LocalDateTime.now())
                    .tipo(MovimientoCaja.Tipo.VENTA).monto(guardada.getTotal()).descripcion("Venta " + guardada.getNumeroComprobante()).build());
        }
        return guardada;
    }

    @Transactional
    public Venta agregarItem(Long ventaId, ItemVentaCreateDTO itemDTO) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no existe: " + ventaId));

        Long productoId = itemDTO.productoId();
        Producto p = productoRepository.findByIdForUpdate(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productoId));

        if (p.getStock() == null || p.getStock() < itemDTO.cantidad()) {
            throw new IllegalStateException("Sin stock suficiente para " + p.getNombre() + 
                ". Stock disponible: " + p.getStock());
        }
        var reglas = reglasOperativasService == null ? null : reglasOperativasService.obtener();
        validarStockVendible(p, itemDTO.cantidad(), reglas);

        ItemVenta nuevoItem = new ItemVenta();
        nuevoItem.setProducto(p);
        nuevoItem.setCantidad(itemDTO.cantidad());
        nuevoItem.setPrecioUnitario(precioAplicable(p, itemDTO.cantidad()));
        nuevoItem.setCostoUnitario(p.getCosto());
        nuevoItem.setUnidadVenta(p.getUnidadVenta());
        nuevoItem.setVenta(venta);
        asignarLotes(nuevoItem, p, itemDTO.cantidad(), reglas);

        p.setStock(p.getStock() - itemDTO.cantidad());
        venta.addItems(nuevoItem);

        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta quitarItem(Long ventaId, Long itemId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no existe: " + ventaId));

        ItemVenta iv = venta.getItems().stream()
                .filter(x -> Objects.equals(x.getId(), itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item no existe: " + itemId));

        Producto p = iv.getProducto();
        p.setStock(p.getStock() + iv.getCantidad());
        restaurarLotes(iv);
        venta.removeItem(iv);

        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta anular(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no existe: " + ventaId));

        if (venta.getEstado() == Venta.Estado.ANULADA) throw new IllegalStateException("La venta ya esta anulada");
        for (ItemVenta iv : venta.getItems()) {
            Producto p = iv.getProducto();
            int anterior = p.getStock();
            p.setStock(p.getStock() + iv.getCantidad());
            restaurarLotes(iv);
            registrarStock(p, iv.getCantidad(), anterior, MovimientoStock.Tipo.ANULACION_VENTA, "VENTA-" + ventaId, venta.getUsuario());
        }
        venta.setEstado(Venta.Estado.ANULADA);
        if (venta.getCaja() != null && venta.getMedioPago() == Venta.MedioPago.EFECTIVO && movimientoCajaRepository != null) {
            movimientoCajaRepository.save(MovimientoCaja.builder().caja(venta.getCaja()).venta(venta).fecha(LocalDateTime.now())
                    .tipo(MovimientoCaja.Tipo.ANULACION).monto(venta.getTotal()).descripcion("Anulacion " + venta.getNumeroComprobante()).build());
        }
        return ventaRepository.save(venta);
    }

    private Venta.MedioPago parseMedioPago(String value) {
        try { return value == null ? Venta.MedioPago.EFECTIVO : Venta.MedioPago.valueOf(value); }
        catch (IllegalArgumentException ex) { throw new IllegalArgumentException("Medio de pago invalido"); }
    }

    private BigDecimal precioAplicable(Producto producto, int cantidad) {
        return producto.getUnidadVenta() == Producto.UnidadVenta.UNIDAD
                && producto.getCantidadMinimaPromo() != null && producto.getPrecioPromocional() != null
                && cantidad >= producto.getCantidadMinimaPromo() ? producto.getPrecioPromocional() : producto.getPrecioVenta();
    }

    private void asignarLotes(ItemVenta item, Producto producto, int cantidad,
                              com.example.demo.dto.ReglasOperativasDTO reglas) {
        if (loteProductoRepository == null) return;
        boolean controlar = reglas == null || reglas.controlarVencimientos();
        boolean bloquearVencidos = reglas == null || reglas.bloquearVentaVencidos();
        var lotes = controlar && bloquearVencidos
                ? loteProductoRepository.disponiblesFefo(producto.getId())
                : loteProductoRepository.disponiblesTodos(producto.getId());
        int pendiente = cantidad;
        for (LoteProducto lote : lotes) {
            if (pendiente == 0) break;
            int usado = Math.min(pendiente, lote.getCantidadDisponible());
            lote.setCantidadDisponible(lote.getCantidadDisponible() - usado); item.asignarLote(lote, usado); pendiente -= usado;
        }
    }

    private void validarStockVendible(Producto producto, int cantidad,
                                      com.example.demo.dto.ReglasOperativasDTO reglas) {
        if (loteProductoRepository == null) return;
        if (reglas != null && !reglas.controlarVencimientos()) return;
        long trazado = loteProductoRepository.cantidadTrazada(producto.getId());
        boolean bloquearVencidos = reglas == null || reglas.bloquearVentaVencidos();
        long disponible = bloquearVencidos
                ? loteProductoRepository.cantidadVendible(producto.getId())
                : trazado;
        long legado = Math.max(0, producto.getStock() - trazado);
        if (legado + disponible < cantidad) {
            throw new IllegalStateException(bloquearVencidos
                    ? "No hay stock vigente suficiente para " + producto.getNombre()
                    : "No hay stock suficiente para " + producto.getNombre());
        }
    }

    private void restaurarLotes(ItemVenta item) {
        if (item.getLotes() == null) return;
        item.getLotes().forEach(a -> {
            LoteProducto lote = a.getLote(); lote.setCantidadDisponible(lote.getCantidadDisponible() + a.getCantidad()); lote.setActivo(true);
        });
    }

    private void registrarStock(Producto producto, int cantidad, int anterior, MovimientoStock.Tipo tipo,
                                String referencia, com.example.demo.models.Persona usuario) {
        if (movimientoStockRepository == null) return;
        movimientoStockRepository.save(MovimientoStock.builder().producto(producto).fecha(LocalDateTime.now()).tipo(tipo)
                .cantidad(cantidad).stockAnterior(anterior).stockNuevo(producto.getStock()).referencia(referencia)
                .descripcion(tipo == MovimientoStock.Tipo.VENTA ? "Salida por venta" : "Reposicion por anulacion").usuario(usuario).build());
    }
}
