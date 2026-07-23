package com.example.demo.services;

import com.example.demo.dto.*;
import com.example.demo.models.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {
    private final CompraRepository compras; private final ProveedorRepository proveedores;
    private final ProductoRepository productos; private final PersonaRepository personas;
    private final MovimientoStockRepository movimientos;
    private final LoteProductoRepository lotes;
    private final HistorialCostoService historialCosto;
    public CompraService(CompraRepository compras, ProveedorRepository proveedores, ProductoRepository productos,
                         PersonaRepository personas, MovimientoStockRepository movimientos, LoteProductoRepository lotes,
                         HistorialCostoService historialCosto) {
        this.compras = compras; this.proveedores = proveedores; this.productos = productos;
        this.personas = personas; this.movimientos = movimientos;
        this.lotes = lotes; this.historialCosto = historialCosto;
    }

    @Transactional
    public CompraDTO crear(CompraCreateDTO dto) {
        Proveedor proveedor = null;
        if (dto.proveedorId() != null) {
            proveedor = proveedores.findById(dto.proveedorId()).filter(Proveedor::isActivo)
                    .orElseThrow(() -> new IllegalArgumentException("Proveedor inexistente o inactivo"));
        }
        Compra compra = Compra.builder().proveedor(proveedor).fecha(LocalDateTime.now()).numeroComprobante(dto.numeroComprobante())
                .usuario(dto.usuarioId() == null ? null : personas.findById(dto.usuarioId()).orElse(null)).build();
        for (ItemCompraCreateDTO itemDTO : dto.items()) {
            Producto producto = productos.findByIdForUpdate(itemDTO.productoId()).orElseThrow(() -> new IllegalArgumentException("Producto inexistente"));
            int anterior = producto.getStock();
            BigDecimal costoAnterior = producto.getCosto();
            producto.setStock(anterior + itemDTO.cantidad()); producto.setCosto(itemDTO.costoUnitario());
            historialCosto.registrar(producto, costoAnterior, itemDTO.costoUnitario(), "COMPRA",
                    dto.numeroComprobante(), dto.usuarioId());
            if (itemDTO.fechaVencimiento() != null) { producto.setVencimiento(true); producto.setFechaVencimiento(itemDTO.fechaVencimiento()); }
            compra.agregarItem(ItemCompra.builder().producto(producto).cantidad(itemDTO.cantidad())
                    .costoUnitario(itemDTO.costoUnitario()).fechaVencimiento(itemDTO.fechaVencimiento()).build());
            movimientos.save(MovimientoStock.builder().producto(producto).fecha(LocalDateTime.now()).tipo(MovimientoStock.Tipo.COMPRA)
                    .cantidad(itemDTO.cantidad()).stockAnterior(anterior).stockNuevo(producto.getStock())
                    .referencia(dto.numeroComprobante()).descripcion("Ingreso por compra").usuario(compra.getUsuario()).build());
        }
        Compra guardada = compras.save(compra);
        for (int index = 0; index < dto.items().size(); index++) {
            ItemCompraCreateDTO itemDTO = dto.items().get(index); ItemCompra item = guardada.getItems().get(index);
            lotes.save(LoteProducto.builder().producto(item.getProducto()).codigoLote(limpiar(itemDTO.codigoLote()))
                    .fechaIngreso(java.time.LocalDate.now()).fechaVencimiento(itemDTO.fechaVencimiento())
                    .cantidadInicial(itemDTO.cantidad()).cantidadDisponible(itemDTO.cantidad()).costoUnitario(itemDTO.costoUnitario())
                    .compra(guardada).activo(true).build());
        }
        return map(guardada);
    }

    @Transactional
    public CompraDTO anular(Long id) {
        Compra compra = compras.findById(id).orElseThrow(() -> new IllegalArgumentException("Compra inexistente"));
        if (compra.getEstado() == Compra.Estado.ANULADA) throw new IllegalStateException("La compra ya esta anulada");
        List<LoteProducto> lotesCompra = lotes.findByCompraId(id);
        if (lotesCompra.stream().anyMatch(l -> !l.getCantidadDisponible().equals(l.getCantidadInicial())))
            throw new IllegalStateException("No se puede anular: parte de los lotes ya fue vendida");
        for (ItemCompra item : compra.getItems()) {
            Producto p = productos.findByIdForUpdate(item.getProducto().getId()).orElseThrow(); int anterior = p.getStock();
            if (anterior < item.getCantidad()) throw new IllegalStateException("No se puede anular: el stock de " + p.getNombre() + " ya fue utilizado");
            p.setStock(anterior - item.getCantidad());
            movimientos.save(MovimientoStock.builder().producto(p).fecha(LocalDateTime.now()).tipo(MovimientoStock.Tipo.ANULACION_COMPRA)
                    .cantidad(-item.getCantidad()).stockAnterior(anterior).stockNuevo(p.getStock()).referencia("COMPRA-"+id)
                    .descripcion("Anulacion de compra").usuario(compra.getUsuario()).build());
        }
        lotesCompra.forEach(l -> { l.setCantidadDisponible(0); l.setActivo(false); });
        compra.setEstado(Compra.Estado.ANULADA); return map(compra);
    }

    @Transactional(readOnly = true) public List<CompraDTO> listar() { return compras.findAllByOrderByFechaDesc().stream().map(this::map).toList(); }
    private CompraDTO map(Compra c) {
        Proveedor p = c.getProveedor();
        ProveedorDTO proveedorDto = p == null ? null
                : new ProveedorDTO(p.getId(), p.getNombre(), p.getCuit(), p.getTelefono(), p.getEmail(), p.getDireccion(), p.isActivo());
        return new CompraDTO(c.getId(), c.getFecha(), c.getNumeroComprobante(), c.getTotal(), c.getEstado().name(),
                proveedorDto,
                c.getItems().stream().map(i -> { LoteProducto lote = lotes.findByCompraId(c.getId()).stream().filter(l -> l.getProducto().getId() == i.getProducto().getId()).findFirst().orElse(null);
                    return new CompraDTO.ItemDTO(i.getProducto().getId(), i.getProducto().getNombre(), i.getCantidad(), i.getCostoUnitario(), i.getSubtotal(), lote == null ? null : lote.getCodigoLote(), lote == null ? i.getFechaVencimiento() : lote.getFechaVencimiento()); }).toList());
    }
    private String limpiar(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
