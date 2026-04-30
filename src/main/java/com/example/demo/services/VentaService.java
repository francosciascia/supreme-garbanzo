package com.example.demo.services;

import com.example.demo.dto.ItemVentaCreateDTO;
import com.example.demo.dto.VentaCreateDTO;
import com.example.demo.models.ItemVenta;
import com.example.demo.models.Producto;
import com.example.demo.models.Venta;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.VentaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
    }

    // ===== Lectura =====
    @Transactional
    public List<Venta> listar() {
        return ventaRepository.findAll();
    }

    @Transactional
    public Optional<Venta> detalle(Long id) {
        return ventaRepository.findById(id);
    }

    // ===== Crear venta con DTO =====
    @Transactional
    public Venta crear(VentaCreateDTO ventaDTO) {
        Venta venta = new Venta();
        venta.setFecha(java.time.LocalDate.now());
        venta.setTotal(java.math.BigDecimal.ZERO);
        
        for (ItemVentaCreateDTO itemDTO : ventaDTO.items()) {
            // 1) Validar y cargar el producto desde DB
            Long productoId = itemDTO.productoId();
            Producto p = productoRepository.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productoId));

            // 2) Stock suficiente
            if (p.getStock() == null || p.getStock() < itemDTO.cantidad()) {
                throw new IllegalStateException("Sin stock suficiente para " + p.getNombre() + 
                    ". Stock disponible: " + p.getStock());
            }

            // 3) Crear item, descontar stock y congelar precio
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
        Producto p = productoRepository.findById(productoId)
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
