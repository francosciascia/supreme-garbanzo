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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    private final ClienteRepository clienteRepository;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                        ClienteRepository clienteRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
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
    public Page<VentaDTO> buscar(Long clienteId, LocalDate desde, LocalDate hasta, Pageable pageable) {
        Specification<Venta> specification = (root, query, cb) -> cb.conjunction();
        if (clienteId != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("cliente").get("id"), clienteId));
        }
        if (desde != null) {
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fecha"), desde));
        }
        if (hasta != null) {
            specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("fecha"), hasta));
        }
        return ventaRepository.findAll(specification, pageable).map(VentaMapper::toDTO);
    }

    // ===== Crear venta con DTO =====
    @Transactional
    public Venta crear(VentaCreateDTO ventaDTO) {
        Venta venta = new Venta();
        venta.setFecha(java.time.LocalDate.now());
        venta.setTotal(java.math.BigDecimal.ZERO);

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

            if (p.getStock() == null || p.getStock() < itemDTO.cantidad()) {
                throw new IllegalStateException("Sin stock suficiente para " + p.getNombre() +
                    ". Stock disponible: " + p.getStock());
            }

            ItemVenta item = new ItemVenta();
            item.setProducto(p);
            item.setCantidad(itemDTO.cantidad());
            item.setPrecioUnitario(p.getPrecioVenta());
            item.setVenta(venta);

            p.setStock(p.getStock() - itemDTO.cantidad());
            venta.addItems(item);
        }

        return ventaRepository.save(venta);
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

        ItemVenta nuevoItem = new ItemVenta();
        nuevoItem.setProducto(p);
        nuevoItem.setCantidad(itemDTO.cantidad());
        nuevoItem.setPrecioUnitario(p.getPrecioVenta());
        nuevoItem.setVenta(venta);

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
        venta.removeItem(iv);

        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta anular(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no existe: " + ventaId));

        for (ItemVenta iv : new ArrayList<>(venta.getItems())) {
            Producto p = iv.getProducto();
            p.setStock(p.getStock() + iv.getCantidad());
            venta.removeItem(iv);
        }
        return ventaRepository.save(venta);
    }
}
