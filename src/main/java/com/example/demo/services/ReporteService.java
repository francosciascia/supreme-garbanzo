package com.example.demo.services;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.models.Venta;
import com.example.demo.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ReporteService {
    private final VentaRepository ventas; private final ProductoService productos;
    public ReporteService(VentaRepository ventas,ProductoService productos){this.ventas=ventas;this.productos=productos;}
    @Transactional(readOnly=true) public Resumen resumen(){List<Venta> validas=ventas.findAll().stream().filter(v->v.getEstado()!=Venta.Estado.ANULADA).toList();LocalDate hoy=LocalDate.now();
        BigDecimal ventasHoy=validas.stream().filter(v->hoy.equals(v.getFecha())).map(Venta::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal ingresos=validas.stream().map(Venta::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);List<ProductoDTO> ps=productos.listar();
        BigDecimal inventario=ps.stream().map(p->p.costo().multiply(BigDecimal.valueOf(p.stock()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal ganancia=validas.stream().flatMap(v->v.getItems().stream()).map(i->i.getPrecioUnitario().subtract(i.getCostoUnitario()).multiply(i.getUnidadVenta()==com.example.demo.models.Producto.UnidadVenta.PESO?BigDecimal.valueOf(i.getCantidad()).movePointLeft(3):BigDecimal.valueOf(i.getCantidad()))).reduce(BigDecimal.ZERO,BigDecimal::add);
        Map<String,BigDecimal> ranking=new HashMap<>();validas.forEach(v->v.getItems().forEach(i->ranking.merge(i.getProducto().getNombre(),i.getUnidadVenta()==com.example.demo.models.Producto.UnidadVenta.PESO?BigDecimal.valueOf(i.getCantidad()).movePointLeft(3):BigDecimal.valueOf(i.getCantidad()),BigDecimal::add)));
        List<Ranking> top=ranking.entrySet().stream().sorted(Map.Entry.<String,BigDecimal>comparingByValue().reversed()).limit(10).map(e->new Ranking(e.getKey(),e.getValue())).toList();
        return new Resumen(ventasHoy,ingresos,ganancia,inventario,ps.stream().filter(p->p.stock()<=p.stockMinimo()).count(),top);}
    public record Resumen(BigDecimal ventasHoy,BigDecimal ingresosTotales,BigDecimal gananciaEstimada,BigDecimal inventarioValorizado,long stockBajo,List<Ranking> masVendidos){}
    public record Ranking(String producto,BigDecimal cantidad){}
    @Transactional(readOnly=true) public String exportarVentasCsv(){StringBuilder csv=new StringBuilder("comprobante,fecha,estado,medio_pago,total,cliente\n");ventas.findAll().forEach(v->{String cliente=v.getCliente()==null?"Consumidor final":v.getCliente().getNombre()+" "+v.getCliente().getApellido();csv.append(value(v.getNumeroComprobante())).append(',').append(v.getFecha()).append(',').append(v.getEstado()).append(',').append(v.getMedioPago()).append(',').append(v.getTotal()).append(',').append(value(cliente)).append('\n');});return csv.toString();}
    private String value(String value){if(value==null)return "";return '"'+value.replace("\"","\"\"")+'"';}
}
