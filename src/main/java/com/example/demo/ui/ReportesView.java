package com.example.demo.ui;
import com.example.demo.services.ReporteService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;import java.nio.charset.StandardCharsets;import java.text.NumberFormat;import java.util.Locale;
@Route(value="reportes",layout=MainLayout.class) @PageTitle("Reportes | Franco")
public class ReportesView extends VerticalLayout{
    private static final NumberFormat MONEY=NumberFormat.getCurrencyInstance(Locale.of("es","AR"));
    public ReportesView(ReporteService service){addClassName("content-view");setSizeFull();var r=service.resumen();Div cards=new Div();cards.addClassName("metric-grid");
        cards.add(metric("Ventas de hoy",MONEY.format(r.ventasHoy())),metric("Ingresos históricos",MONEY.format(r.ingresosTotales())),metric("Ganancia estimada",MONEY.format(r.gananciaEstimada())),metric("Inventario valorizado",MONEY.format(r.inventarioValorizado())),metric("Productos con stock bajo",r.stockBajo()));
        Anchor export=new Anchor(new StreamResource("ventas.csv",()->new ByteArrayInputStream(service.exportarVentasCsv().getBytes(StandardCharsets.UTF_8))),"Exportar ventas CSV");export.getElement().setAttribute("download",true);
        Grid<ReporteService.Ranking> top=new Grid<>(ReporteService.Ranking.class,false);top.addColumn(ReporteService.Ranking::producto).setHeader("Producto");top.addColumn(ReporteService.Ranking::cantidad).setHeader("Unidades vendidas");top.setItems(r.masVendidos());add(new H2("Reportes del negocio"),export,cards,new H3("Productos más vendidos"),top);expand(top);}
    private Div metric(String label,Object value){Div d=new Div(new Span(label),new H3(String.valueOf(value)));d.addClassName("metric-card");return d;}
}
