package com.example.demo.services;
import com.example.demo.dto.MovimientoStockDTO;
import com.example.demo.models.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioService {
    private final ProductoRepository productos; private final MovimientoStockRepository movimientos; private final PersonaRepository personas;
    public InventarioService(ProductoRepository productos,MovimientoStockRepository movimientos,PersonaRepository personas){this.productos=productos;this.movimientos=movimientos;this.personas=personas;}
    @Transactional public MovimientoStockDTO ajustar(Long productoId,int stockNuevo,String motivo,Long usuarioId){
        if(stockNuevo<0)throw new IllegalArgumentException("El stock no puede ser negativo");if(motivo==null||motivo.isBlank())throw new IllegalArgumentException("Indicá el motivo del ajuste");
        Producto p=productos.findByIdForUpdate(productoId).orElseThrow(()->new IllegalArgumentException("Producto inexistente"));int anterior=p.getStock();p.setStock(stockNuevo);
        Persona usuario=usuarioId==null?null:personas.findById(usuarioId).orElse(null);MovimientoStock m=movimientos.save(MovimientoStock.builder().producto(p).fecha(LocalDateTime.now()).tipo(MovimientoStock.Tipo.AJUSTE)
                .cantidad(stockNuevo-anterior).stockAnterior(anterior).stockNuevo(stockNuevo).descripcion(motivo.trim()).usuario(usuario).build());return dto(m);}
    @Transactional(readOnly=true) public List<MovimientoStockDTO> listar(){return movimientos.findTop100ByOrderByFechaDesc().stream().map(this::dto).toList();}
    private MovimientoStockDTO dto(MovimientoStock m){return new MovimientoStockDTO(m.getId(),m.getFecha(),m.getProducto().getId(),m.getProducto().getNombre(),m.getTipo().name(),m.getCantidad(),m.getStockAnterior(),m.getStockNuevo(),m.getReferencia(),m.getDescripcion(),m.getUsuario()==null?null:m.getUsuario().getNombre()+" "+m.getUsuario().getApellido());}
}
