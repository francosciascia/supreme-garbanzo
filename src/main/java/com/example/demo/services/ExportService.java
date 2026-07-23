package com.example.demo.services;

import com.example.demo.dto.ClienteDTO;
import com.example.demo.dto.ProductoDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExportService {
    private final ProductoService productos;
    private final ClienteService clientes;
    private final InventarioService inventario;
    private final ReporteService reportes;

    public ExportService(ProductoService productos, ClienteService clientes, InventarioService inventario,
                         ReporteService reportes) {
        this.productos = productos;
        this.clientes = clientes;
        this.inventario = inventario;
        this.reportes = reportes;
    }

    @Transactional(readOnly = true)
    public String productosCsv() {
        StringBuilder csv = new StringBuilder(
                "nombre,codigo_barras,marca,costo,precio,stock,unidad,alicuota_iva\n");
        for (ProductoDTO p : productos.listar()) {
            csv.append(q(p.nombre())).append(',')
                    .append(q(p.codigoBarras())).append(',')
                    .append(q(p.marca())).append(',')
                    .append(p.costo()).append(',')
                    .append(p.precioVenta()).append(',')
                    .append(p.stock()).append(',')
                    .append(p.unidadVenta()).append(',')
                    .append(p.alicuotaIva()).append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String clientesCsv() {
        StringBuilder csv = new StringBuilder("id,nombre,apellido,dni,email,telefono,saldo,limite_credito,activo\n");
        for (ClienteDTO c : clientes.listar()) {
            csv.append(c.id()).append(',')
                    .append(q(c.nombre())).append(',')
                    .append(q(c.apellido())).append(',')
                    .append(c.dni() == null ? "" : c.dni()).append(',')
                    .append(q(c.email())).append(',')
                    .append(q(c.telefono())).append(',')
                    .append(c.saldoCuenta()).append(',')
                    .append(c.limiteCredito()).append(',')
                    .append(c.activo()).append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String stockCsv() {
        StringBuilder csv = new StringBuilder("producto,stock,unidad,costo,precio_venta,stock_minimo,bajo\n");
        for (ProductoDTO p : productos.listar()) {
            csv.append(q(p.nombre())).append(',')
                    .append(p.stock()).append(',')
                    .append(p.unidadVenta()).append(',')
                    .append(p.costo()).append(',')
                    .append(p.precioVenta()).append(',')
                    .append(p.stockMinimo()).append(',')
                    .append(p.stock() != null && p.stock() <= p.stockMinimo()).append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String movimientosStockCsv() {
        StringBuilder csv = new StringBuilder("fecha,producto,tipo,cantidad,stock_anterior,stock_nuevo,referencia\n");
        for (var m : inventario.listar()) {
            csv.append(m.fecha()).append(',')
                    .append(q(m.producto())).append(',')
                    .append(m.tipo()).append(',')
                    .append(m.cantidad()).append(',')
                    .append(m.stockAnterior()).append(',')
                    .append(m.stockNuevo()).append(',')
                    .append(q(m.referencia())).append('\n');
        }
        return csv.toString();
    }

    @Transactional(readOnly = true)
    public String ventasCsv() {
        return reportes.exportarVentasCsv();
    }

    private String q(String value) {
        if (value == null) return "";
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
