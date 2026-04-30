package com.example.demo.services;

import com.example.demo.dto.ProductoDTO;
import com.example.demo.dto.ProductoCUDTO;
import com.example.demo.models.Producto;
import com.example.demo.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository){
        this.productoRepository = productoRepository;
    }

    // Vista ---------------
    @Transactional
    public List<ProductoDTO> listar(){
        return productoRepository.findAll()
                .stream()
                .map(p -> new ProductoDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getDescripcion(),
                        p.getStock(),
                        p.isVencimiento(),
                        p.getCosto(),
                        p.getPrecioVenta()
                ))
                .toList();
    }

    @Transactional
    public Optional<ProductoDTO> detalle(Long id){
        return productoRepository.findById(id)
                .map(p -> new ProductoDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getDescripcion(),
                        p.getStock(),
                        p.isVencimiento(),
                        p.getCosto(),
                        p.getPrecioVenta()
                ));
    }

    // Escritura

    @Transactional
    public ProductoDTO crear(ProductoCUDTO productoDTO){
        validarPreciosCosto(productoDTO.costo(), productoDTO.precioVenta());
        
        Producto producto = Producto.builder()
                .nombre(productoDTO.nombre())
                .descripcion(productoDTO.descripcion())
                .stock(productoDTO.stock())
                .vencimiento(productoDTO.vencimiento())
                .costo(productoDTO.costo())
                .precioVenta(productoDTO.precioVenta())
                .build();
        
        Producto guardado = productoRepository.save(producto);
        
        return new ProductoDTO(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getDescripcion(),
                guardado.getStock(),
                guardado.isVencimiento(),
                guardado.getCosto(),
                guardado.getPrecioVenta()
        );
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoCUDTO cambios) {
        validarPreciosCosto(cambios.costo(), cambios.precioVenta());
        
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + id));

        if (cambios.nombre() != null) p.setNombre(cambios.nombre());
        if (cambios.costo() != null) p.setCosto(cambios.costo());
        if (cambios.stock() != null) p.setStock(cambios.stock());
        if (cambios.precioVenta() != null) p.setPrecioVenta(cambios.precioVenta());
        if (cambios.descripcion() != null) p.setDescripcion(cambios.descripcion());

        Producto actualizado = productoRepository.save(p);
        
        return new ProductoDTO(
                actualizado.getId(),
                actualizado.getNombre(),
                actualizado.getDescripcion(),
                actualizado.getStock(),
                actualizado.isVencimiento(),
                actualizado.getCosto(),
                actualizado.getPrecioVenta()
        );
    }

    @Transactional
    public void eliminar(Long id){
        if (!productoRepository.existsById(id)){
            throw new IllegalArgumentException("Producto no existe: "+id);
        }
        productoRepository.deleteById(id);
    }

    // Validaciones
    private void validarPreciosCosto(java.math.BigDecimal costo, java.math.BigDecimal precioVenta) {
        if (precioVenta != null && costo != null && precioVenta.compareTo(costo) < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser menor que el costo");
        }
    }
}
