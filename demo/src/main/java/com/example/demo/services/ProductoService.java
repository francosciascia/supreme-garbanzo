package com.example.demo.services;

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
    public List<Producto> listar(){
        return productoRepository.findAll();
    }

    @Transactional
    public Optional<Producto> detalle (Long id){
        return productoRepository.findById(id);
    }

    // Escritura

    @Transactional
    public Producto crear (Producto producto){
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizar(Long id, Producto cambios) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + id));

        if (cambios.getNombre() != null) p.setNombre(cambios.getNombre());
        if (cambios.getCosto() != null) p.setCosto(cambios.getCosto());
        if (cambios.getStock() != null) p.setStock(cambios.getStock());
        if (cambios.getPrecioVenta() != null) p.setPrecioVenta(cambios.getPrecioVenta());
        if (cambios.getDescripcion() != null) p.setDescripcion(cambios.getDescripcion());

        return productoRepository.save(p);
    }

    @Transactional
    public void eliminar(Long id){
        if (!productoRepository.existsById(id)){
            throw new IllegalArgumentException("Producto no existe: "+id);

        }
        productoRepository.deleteById(id);
    }

}
