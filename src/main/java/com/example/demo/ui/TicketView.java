package com.example.demo.ui;

import com.example.demo.dto.ConfiguracionComercioDTO;
import com.example.demo.dto.ItemVentaDTO;
import com.example.demo.dto.VentaDTO;
import com.example.demo.services.ConfiguracionComercioService;
import com.example.demo.services.VentaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

@Route("ticket/:id")
@PageTitle("Comprobante")
public class TicketView extends VerticalLayout implements BeforeEnterObserver {
    private static final Locale LOCALE = Locale.of("es", "AR");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final VentaService ventas;
    private final ConfiguracionComercioService configuracion;

    public TicketView(VentaService ventas, ConfiguracionComercioService configuracion) {
        this.ventas = ventas;
        this.configuracion = configuracion;
        addClassName("ticket-page");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!UserSession.isLoggedIn()) {
            event.rerouteTo(LoginView.class);
            return;
        }
        Long id;
        try {
            id = Long.valueOf(event.getRouteParameters().get("id").orElseThrow());
        } catch (RuntimeException exception) {
            event.rerouteTo(VentasView.class);
            return;
        }
        VentaDTO sale = ventas.detalleDTO(id).orElse(null);
        if (sale == null) {
            event.rerouteTo(VentasView.class);
            return;
        }
        render(sale, configuracion.obtener());
    }

    private void render(VentaDTO sale, ConfiguracionComercioDTO config) {
        removeAll();
        int width = config.anchoTicket() == null ? 80 : config.anchoTicket();
        getStyle().set("--ticket-width", width + "mm");
        getUI().ifPresent(ui -> ui.getPage().executeJs(
                "document.documentElement.style.setProperty('--ticket-width', $0)", width + "mm"));
        NumberFormat money = money(config.moneda());

        Div ticket = new Div();
        ticket.addClassName("thermal-ticket");
        if (config.logoUrl() != null && !config.logoUrl().isBlank()) {
            Image logo = new Image(config.logoUrl(), config.nombre());
            logo.addClassName("ticket-logo");
            ticket.add(logo);
        }
        ticket.add(new H2(text(config.nombre(), "Comercio")));
        addIfPresent(ticket, config.encabezadoTicket(), "ticket-header");
        if (config.mostrarDatosFiscalesTicket()) {
            addIfPresent(ticket, config.razonSocial(), null);
            addIfPresent(ticket, config.cuit() == null ? null : "CUIT: " + config.cuit(), null);
            addIfPresent(ticket, config.condicionIva(), null);
        }
        addIfPresent(ticket, config.direccion(), null);
        addIfPresent(ticket, config.telefono() == null ? null : "Tel: " + config.telefono(), null);

        ticket.add(separator(), line("Comprobante", text(sale.numeroComprobante(), "Venta " + sale.id())),
                line("Fecha", DATE.format(sale.fecha())));
        if (sale.cliente() != null)
            ticket.add(line("Cliente", sale.cliente().nombre() + " " + sale.cliente().apellido()));
        ticket.add(separator());

        Div items = new Div();
        items.addClassName("ticket-items");
        sale.items().forEach(item -> items.add(item(item, money)));
        ticket.add(items);
        if (sale.descuento().signum() > 0) ticket.add(line("Descuento", "-" + money.format(sale.descuento())));
        ticket.add(separator());
        if (config.mostrarIvaTicket()) {
            BigDecimal neto = BigDecimal.ZERO;
            BigDecimal iva = BigDecimal.ZERO;
            for (ItemVentaDTO item : sale.items()) {
                BigDecimal rate = item.alicuotaIva() == null ? new BigDecimal("21") : item.alicuotaIva();
                BigDecimal divisor = BigDecimal.valueOf(100).add(rate);
                BigDecimal itemNeto = item.subtotal().multiply(BigDecimal.valueOf(100)).divide(divisor, 2, RoundingMode.HALF_UP);
                neto = neto.add(itemNeto);
                iva = iva.add(item.subtotal().subtract(itemNeto));
            }
            if (sale.descuento().signum() > 0) {
                BigDecimal factor = sale.total().divide(sale.total().add(sale.descuento()), 8, RoundingMode.HALF_UP);
                neto = neto.multiply(factor).setScale(2, RoundingMode.HALF_UP);
                iva = sale.total().subtract(neto);
            }
            ticket.add(line("Neto", money.format(neto)), line("IVA", money.format(iva)));
        }
        Div total = line("TOTAL", money.format(sale.total()));
        total.addClassName("ticket-total");
        ticket.add(total, line("Pago", sale.medioPago().replace('_', ' ')));
        if (sale.montoRecibido() != null && "EFECTIVO".equals(sale.medioPago()))
            ticket.add(line("Recibido", money.format(sale.montoRecibido())));
        if (sale.vuelto().signum() > 0) ticket.add(line("Vuelto", money.format(sale.vuelto())));
        if ("ANULADA".equals(sale.estado())) {
            Paragraph cancelled = new Paragraph("VENTA ANULADA");
            cancelled.addClassName("ticket-cancelled");
            ticket.add(cancelled);
        }
        if (config.mensajeTicket() != null && !config.mensajeTicket().isBlank()) {
            ticket.add(separator());
            addIfPresent(ticket, config.mensajeTicket(), "ticket-footer");
        }

        Button back = new Button("Volver a ventas", event -> getUI().ifPresent(ui -> ui.navigate(VentasView.class)));
        Button print = new Button("Imprimir", event -> getUI().ifPresent(ui -> ui.getPage().executeJs("window.print()")));
        HorizontalLayout actions = new HorizontalLayout(back, print);
        actions.addClassName("no-print");
        add(ticket, actions);
    }

    private Div item(ItemVentaDTO item, NumberFormat money) {
        Div row = new Div();
        row.addClassName("ticket-item");
        Span name = new Span(item.nombreProducto());
        String unit = "PESO".equals(item.unidadVenta()) ? " kg" : " u";
        Span calculation = new Span(item.cantidadMostrada().stripTrailingZeros().toPlainString() + unit
                + " × " + money.format(item.precioUnitario()) + " = " + money.format(item.subtotal()));
        row.add(name, calculation);
        return row;
    }

    private Div line(String label, String value) {
        Div line = new Div(new Span(label), new Span(value));
        line.addClassName("ticket-line");
        return line;
    }

    private Hr separator() { Hr hr = new Hr(); hr.addClassName("ticket-separator"); return hr; }
    private void addIfPresent(Div target, String value, String className) {
        if (value == null || value.isBlank()) return;
        Paragraph paragraph = new Paragraph(value);
        if (className != null) paragraph.addClassName(className);
        target.add(paragraph);
    }
    private NumberFormat money(String code) {
        NumberFormat format = NumberFormat.getCurrencyInstance(LOCALE);
        try { format.setCurrency(Currency.getInstance(code)); } catch (RuntimeException ignored) { }
        return format;
    }
    private String text(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
}
