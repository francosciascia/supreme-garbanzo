package com.example.demo.services;

import com.example.demo.dto.CategoriaDTO;
import com.example.demo.dto.ProductoCUDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.models.Categoria;
import com.example.demo.models.Producto;
import com.example.demo.models.Producto.UnidadVenta;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ReglasOperativasService reglas;
    private final HistorialCostoService historialCosto;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository,
                           ReglasOperativasService reglas, HistorialCostoService historialCosto) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.reglas = reglas;
        this.historialCosto = historialCosto;
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listar() {
        return productoRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoDTO> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoria_Id(categoriaId).stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProductoDTO> detalle(Long id) {
        return productoRepository.findById(id).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscar(String search, Long categoriaId, Pageable pageable) {
        String normalized = search == null ? "" : search.trim();
        return productoRepository.buscar(normalized, categoriaId, pageable).map(this::mapToDTO);
    }

    private ProductoDTO mapToDTO(Producto p) {
        CategoriaDTO categDTO = p.getCategoria() != null
                ? new CategoriaDTO(p.getCategoria().getId(), p.getCategoria().getNombre(), p.getCategoria().getDescripcion())
                : null;
        return new ProductoDTO(
                p.getId(), p.getNombre(), p.getDescripcion(), p.getStock(), p.isVencimiento(),
                p.getCosto(), p.getPrecioVenta(), categDTO, p.getCodigoBarras(), p.getMarca(),
                p.getStockMinimo(), p.getUnidadVenta().name(), p.getFechaVencimiento(),
                p.getCantidadMinimaPromo(), p.getPrecioPromocional(),
                p.getAlicuotaIva() == null ? new BigDecimal("21") : p.getAlicuotaIva());
    }

    @Transactional
    public ProductoDTO crear(ProductoCUDTO productoDTO) {
        return crear(productoDTO, null);
    }

    @Transactional
    public ProductoDTO crear(ProductoCUDTO productoDTO, Long usuarioId) {
        validarPreciosCosto(productoDTO.costo(), productoDTO.precioVenta());
        validarPromocion(productoDTO.cantidadMinimaPromo(), productoDTO.precioPromocional());
        validarCodigoBarras(productoDTO.codigoBarras(), null);

        Categoria categoria = null;
        if (productoDTO.categoriaId() != null) {
            categoria = categoriaRepository.findById(productoDTO.categoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        }

        Producto producto = Producto.builder()
                .nombre(productoDTO.nombre())
                .descripcion(productoDTO.descripcion())
                .codigoBarras(limpiar(productoDTO.codigoBarras()))
                .marca(limpiar(productoDTO.marca()))
                .stock(productoDTO.stock())
                .stockMinimo(productoDTO.stockMinimo() == null ? 5 : productoDTO.stockMinimo())
                .unidadVenta(parseUnidad(productoDTO.unidadVenta()))
                .vencimiento(productoDTO.vencimiento())
                .fechaVencimiento(productoDTO.fechaVencimiento())
                .cantidadMinimaPromo(productoDTO.cantidadMinimaPromo())
                .precioPromocional(productoDTO.precioPromocional())
                .costo(productoDTO.costo())
                .precioVenta(productoDTO.precioVenta())
                .alicuotaIva(alicuota(productoDTO.alicuotaIva()))
                .categoria(categoria)
                .build();

        Producto guardado = productoRepository.save(producto);
        historialCosto.registrar(guardado, null, guardado.getCosto(), "ALTA", null, usuarioId);
        return mapToDTO(guardado);
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoCUDTO cambios) {
        return actualizar(id, cambios, null);
    }

    @Transactional
    public ProductoDTO actualizar(Long id, ProductoCUDTO cambios, Long usuarioId) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no existe: " + id));
        validarPreciosCosto(cambios.costo(), cambios.precioVenta());
        validarPromocion(cambios.cantidadMinimaPromo(), cambios.precioPromocional());
        validarCodigoBarras(cambios.codigoBarras(), id);
        BigDecimal costoAnterior = p.getCosto();

        if (cambios.nombre() != null) p.setNombre(cambios.nombre());
        if (cambios.costo() != null) p.setCosto(cambios.costo());
        if (cambios.stock() != null) p.setStock(cambios.stock());
        if (cambios.precioVenta() != null) p.setPrecioVenta(cambios.precioVenta());
        if (cambios.descripcion() != null) p.setDescripcion(cambios.descripcion());
        if (cambios.vencimiento() != null) p.setVencimiento(cambios.vencimiento());
        p.setCodigoBarras(limpiar(cambios.codigoBarras()));
        p.setMarca(limpiar(cambios.marca()));
        if (cambios.stockMinimo() != null) p.setStockMinimo(cambios.stockMinimo());
        if (cambios.unidadVenta() != null) p.setUnidadVenta(parseUnidad(cambios.unidadVenta()));
        p.setFechaVencimiento(cambios.fechaVencimiento());
        p.setCantidadMinimaPromo(cambios.cantidadMinimaPromo());
        p.setPrecioPromocional(cambios.precioPromocional());
        if (cambios.alicuotaIva() != null) p.setAlicuotaIva(alicuota(cambios.alicuotaIva()));

        if (cambios.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(cambios.categoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
            p.setCategoria(categoria);
        }

        Producto actualizado = productoRepository.save(p);
        historialCosto.registrar(actualizado, costoAnterior, actualizado.getCosto(), "MANUAL", null, usuarioId);
        return mapToDTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new IllegalArgumentException("Producto no existe: " + id);
        }
        productoRepository.deleteById(id);
    }

    @Transactional
    public int actualizarPrecios(BigDecimal porcentaje, Long categoriaId) {
        if (porcentaje == null || porcentaje.compareTo(new BigDecimal("-100")) <= 0)
            throw new IllegalArgumentException("El porcentaje debe ser mayor a -100");
        BigDecimal factor = BigDecimal.ONE.add(porcentaje.movePointLeft(2));
        List<Producto> productos = categoriaId == null
                ? productoRepository.findAll()
                : productoRepository.findByCategoria_Id(categoriaId);
        productos.forEach(p -> p.setPrecioVenta(p.getPrecioVenta().multiply(factor).setScale(2, RoundingMode.HALF_UP)));
        return productos.size();
    }

    private void validarPreciosCosto(BigDecimal costo, BigDecimal precioVenta) {
        if (precioVenta != null && costo != null && precioVenta.compareTo(costo) < 0) {
            throw new IllegalArgumentException("El precio de venta no puede ser menor que el costo");
        }
        if (reglas == null || precioVenta == null || costo == null || precioVenta.signum() <= 0) return;
        BigDecimal minimo = reglas.obtener().margenMinimoPct();
        if (minimo != null && minimo.signum() > 0) {
            BigDecimal margen = precioVenta.subtract(costo).multiply(BigDecimal.valueOf(100))
                    .divide(precioVenta, 2, RoundingMode.HALF_UP);
            if (margen.compareTo(minimo) < 0)
                throw new IllegalArgumentException("El margen (" + margen + "%) es menor al mínimo (" + minimo + "%)");
        }
    }

    private void validarPromocion(Integer cantidad, BigDecimal precio) {
        if ((cantidad == null) != (precio == null))
            throw new IllegalArgumentException("Completá cantidad y precio promocional");
        if (cantidad != null && cantidad < 2)
            throw new IllegalArgumentException("La promoción debe comenzar desde 2 unidades");
    }

    @Transactional(readOnly = true)
    public Optional<ProductoDTO> buscarPorCodigo(String codigo) {
        return productoRepository.findByCodigoBarras(codigo == null ? "" : codigo.trim()).map(this::mapToDTO);
    }

    private void validarCodigoBarras(String codigo, Long productoId) {
        String value = limpiar(codigo);
        if (value == null) return;
        productoRepository.findByCodigoBarras(value).filter(p -> !java.util.Objects.equals(p.getId(), productoId))
                .ifPresent(p -> { throw new IllegalArgumentException("El codigo de barras ya esta registrado"); });
    }

    private UnidadVenta parseUnidad(String value) {
        try {
            return value == null ? UnidadVenta.UNIDAD : UnidadVenta.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unidad de venta invalida");
        }
    }

    private BigDecimal alicuota(BigDecimal value) {
        if (value == null) return new BigDecimal("21");
        if (value.signum() < 0 || value.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Alícuota de IVA inválida");
        return value;
    }

    private String limpiar(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }
}
