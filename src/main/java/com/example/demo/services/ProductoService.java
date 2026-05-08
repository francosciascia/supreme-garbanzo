package com.example.demo.services;

import com.example.demo.dto.CategoriaDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.dto.ProductoCUDTO;
import com.example.demo.models.Categoria;
import com.example.demo.models.Producto;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository){
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Vista ---------------
    @Transactional
    public List<ProductoDTO> listar(){
        return productoRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public List<ProductoDTO> listarPorCategoria(Long categoriaId){
        return productoRepository.findByCategoria_Id(categoriaId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional
    public Optional<ProductoDTO> detalle(Long id){
        return productoRepository.findById(id)
                .map(this::mapToDTO);
    }

    private ProductoDTO mapToDTO(Producto p) {
        CategoriaDTO categDTO = p.getCategoria() != null ?
                new CategoriaDTO(p.getCategoria().getId(), p.getCategoria().getNombre(), p.getCategoria().getDescripcion())
                : null;

        return new ProductoDTO(
                p.getId(),
                p.getNombre(),
                p.getDescripcion(),
                p.getStock(),
                p.isVencimiento(),
                p.getCosto(),
                p.getPrecioVenta(),
                categDTO
        );
    }

    // Escritura

    @Transactional
    public ProductoDTO crear(ProductoCUDTO productoDTO){
        validarPreciosCosto(productoDTO.costo(), productoDTO.precioVenta());
        
        Categoria categoria = null;
        if (productoDTO.categoriaId() != null) {
            categoria = categoriaRepository.findById(productoDTO.categoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        }
        
        Producto producto = Producto.builder()
                .nombre(productoDTO.nombre())
                .descripcion(productoDTO.descripcion())
                .stock(productoDTO.stock())
                .vencimiento(productoDTO.vencimiento())
                .costo(productoDTO.costo())
                .precioVenta(productoDTO.precioVenta())
                .categoria(categoria)
                .build();
        
        Producto guardado = productoRepository.save(producto);
        
        return mapToDTO(guardado);
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
        
        if (cambios.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(cambios.categoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            p.setCategoria(categoria);
        }

        Producto actualizado = productoRepository.save(p);
        
        return mapToDTO(actualizado);
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
