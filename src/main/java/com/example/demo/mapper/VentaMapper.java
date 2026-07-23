package com.example.demo.mapper;

import com.example.demo.dto.ItemVentaDTO;
import com.example.demo.dto.VentaDTO;
import com.example.demo.models.Cliente;
import com.example.demo.models.ItemVenta;
import com.example.demo.models.Venta;

import java.util.List;

public final class VentaMapper {

    private VentaMapper() {}

    public static VentaDTO toDTO(Venta venta) {
        if (venta == null) return null;

        List<ItemVentaDTO> items = venta.getItems() == null ? List.of()
                : venta.getItems().stream()
                        .map(VentaMapper::itemToDTO)
                        .toList();

        return new VentaDTO(
                venta.getId(),
                venta.getFecha(),
                venta.getTotal(),
                clienteResumen(venta.getCliente()),
                items,
                venta.getEstado().name(),
                venta.getMedioPago().name(),
                venta.getMontoRecibido(),
                venta.getVuelto(),
                venta.getDescuento(),
                venta.getNumeroComprobante()
        );
    }

    private static ItemVentaDTO itemToDTO(ItemVenta item) {
        java.math.BigDecimal alicuota = item.getProducto() != null && item.getProducto().getAlicuotaIva() != null
                ? item.getProducto().getAlicuotaIva()
                : new java.math.BigDecimal("21");
        return new ItemVentaDTO(
                item.getId(),
                item.getProducto() != null ? item.getProducto().getId() : null,
                item.getProducto() != null ? item.getProducto().getNombre() : null,
                item.getCantidad(),
                item.getUnidadVenta() == com.example.demo.models.Producto.UnidadVenta.PESO
                        ? java.math.BigDecimal.valueOf(item.getCantidad()).movePointLeft(3)
                        : java.math.BigDecimal.valueOf(item.getCantidad()),
                item.getUnidadVenta().name(),
                item.getPrecioUnitario(),
                item.getSubtotal(),
                alicuota
        );
    }

    private static VentaDTO.ClienteResumenDTO clienteResumen(Cliente cliente) {
        if (cliente == null) return null;
        return new VentaDTO.ClienteResumenDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDni()
        );
    }
}
