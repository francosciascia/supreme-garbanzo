package com.example.demo.ui;

import com.example.demo.dto.ProductoDTO;
import com.example.demo.models.Venta;
import com.example.demo.services.ClienteService;
import com.example.demo.services.ProductoService;
import com.example.demo.services.VentaService;
import com.example.demo.services.LoteService;
import com.example.demo.services.ReglasOperativasService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Franco")
public class DashboardView extends VerticalLayout {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));

    public DashboardView(ProductoService productoService, ClienteService clienteService, VentaService ventaService,
                         LoteService loteService, ReglasOperativasService reglasService) {
        addClassName("content-view");
        setSizeFull();
        List<ProductoDTO> products = productoService.listar();
        List<Venta> sales = ventaService.listar();
        long lowStock = products.stream().filter(product -> product.stock() != null && product.stock() <= product.stockMinimo()).count();
        BigDecimal revenue = sales.stream().filter(sale -> sale.getEstado() != Venta.Estado.ANULADA).map(Venta::getTotal).filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int expirationDays = reglasService.obtener().diasAlertaVencimiento();

        Div cards = new Div();
        cards.addClassName("metric-grid");
        cards.add(metric("Productos", products.size()), metric("Ventas", sales.size()),
                metric("Ingresos", CURRENCY.format(revenue)), metric("Stock bajo", lowStock),
                metric("Vencen en " + expirationDays + " días", loteService.proximosAVencer(expirationDays).size()),
                metric("Clientes", clienteService.listar().size()));

        Grid<ProductoDTO> stockGrid = new Grid<>(ProductoDTO.class, false);
        stockGrid.addColumn(ProductoDTO::nombre).setHeader("Producto").setAutoWidth(true);
        stockGrid.addColumn(ProductoDTO::stock).setHeader("Stock");
        stockGrid.addColumn(product -> product.categoria() == null ? "Sin categoría" : product.categoria().nombre())
                .setHeader("Categoría").setAutoWidth(true);
        stockGrid.setItems(products.stream().filter(product -> product.stock() != null && product.stock() <= product.stockMinimo()).toList());
        stockGrid.setHeight("280px");
        add(new H2("Resumen general"), cards, new H3("Productos con stock bajo"), stockGrid);
    }

    private Div metric(String label, Object value) {
        Div card = new Div(new Span(label), new H3(String.valueOf(value)));
        card.addClassName("metric-card");
        return card;
    }
}
