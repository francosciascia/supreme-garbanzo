package com.example.demo.services;

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

    // ===== Crear venta (sin DTOs) =====
    @Transactional
    public Venta crear(Venta venta) {
        for (ItemVenta item : venta.getItems()) {
            // 1) Validar y cargar el producto desde DB
            Long productoId = Objects.requireNonNull(item.getProducto(), "Falta 'producto' en el ítem").getId();
            Producto p = productoRepository.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productoId));

            // 2) Stock suficiente
            if (p.getStock() == null || p.getStock() < item.getCantidad()) {
                throw new IllegalStateException("Sin stock suficiente para " + p.getNombre());
            }

            // 3) Descontar stock y “congelar” precio
            p.setStock(p.getStock() - item.getCantidad());
            item.setPrecioUnitario(p.getPrecioVenta());


            item.setVenta(venta);
        }
        // cascade = ALL en Venta → persiste también los items
        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta agregarItem(Long ventaId, ItemVenta nuevoItem) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new IllegalArgumentException("Venta no existe: " + ventaId));

        Long productoId = Objects.requireNonNull(nuevoItem.getProducto(), "Falta 'producto' en el ítem").getId();
        Producto p = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + productoId));

        if (p.getStock() == null || p.getStock() < nuevoItem.getCantidad()) {
            throw new IllegalStateException("Sin stock suficiente para " + p.getNombre());
        }

        p.setStock(p.getStock() - nuevoItem.getCantidad());
        nuevoItem.setPrecioUnitario(p.getPrecioVenta());
        nuevoItem.setVenta(venta);

        venta.removeItem(nuevoItem);

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
