package com.example.demo.ui;

import com.example.demo.dto.CajaDTO;
import com.example.demo.dto.CierreCajaResumenDTO;
import com.example.demo.dto.MovimientoCajaDTO;
import com.example.demo.models.MovimientoCaja;
import com.example.demo.services.CajaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "caja", layout = MainLayout.class)
@PageTitle("Caja | Franco")
public class CajaView extends VerticalLayout {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));
    private final CajaService service;
    private final H2 status = new H2();
    private final Grid<MovimientoCajaDTO> grid = new Grid<>(MovimientoCajaDTO.class, false);

    public CajaView(CajaService service) {
        this.service = service;
        addClassName("content-view");
        setSizeFull();
        grid.addColumn(MovimientoCajaDTO::fecha).setHeader("Fecha");
        grid.addColumn(MovimientoCajaDTO::tipo).setHeader("Tipo");
        grid.addColumn(m -> MONEY.format(m.monto())).setHeader("Monto");
        grid.addColumn(MovimientoCajaDTO::descripcion).setHeader("Detalle");
        Button open = new Button("Abrir caja", e -> abrir());
        Button income = new Button("Ingreso", e -> movimiento(MovimientoCaja.Tipo.INGRESO));
        Button withdrawal = new Button("Retiro", e -> movimiento(MovimientoCaja.Tipo.RETIRO));
        Button close = new Button("Cerrar caja", e -> cerrar());
        open.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(status, new HorizontalLayout(open, income, withdrawal, close), grid);
        expand(grid);
        refresh();
    }

    private Long userId() {
        return UserSession.getUser().id();
    }

    private void refresh() {
        service.activa(userId()).ifPresentOrElse(c -> {
            status.setText("Caja abierta #" + c.id() + " · inicial " + MONEY.format(c.montoInicial()));
            grid.setItems(service.movimientos(c.id()));
        }, () -> {
            status.setText("No hay una caja abierta");
            grid.setItems();
        });
    }

    private void abrir() {
        amountDialog("Monto inicial", amount -> {
            service.abrir(userId(), amount);
            refresh();
        });
    }

    private void movimiento(MovimientoCaja.Tipo type) {
        service.activa(userId()).ifPresentOrElse(c -> amountDialog(
                type == MovimientoCaja.Tipo.INGRESO ? "Ingreso" : "Retiro",
                amount -> {
                    service.movimiento(c.id(), type, amount, "Movimiento manual");
                    refresh();
                }), () -> ViewSupport.error(new IllegalStateException("Primero abrí la caja")));
    }

    private void cerrar() {
        service.activa(userId()).ifPresentOrElse(c -> {
            CierreCajaResumenDTO resumen = service.resumenCierre(c.id());
            Dialog d = new Dialog();
            d.setHeaderTitle("Cierre de caja #" + c.id());
            d.setWidth("min(520px, 95vw)");
            BigDecimalField counted = new BigDecimalField("Efectivo contado");
            counted.setWidthFull();
            counted.setValue(resumen.esperado());
            Paragraph preview = new Paragraph();
            Runnable update = () -> {
                BigDecimal real = counted.getValue() == null ? BigDecimal.ZERO : counted.getValue();
                preview.setText("Diferencia estimada: " + MONEY.format(real.subtract(resumen.esperado())));
            };
            counted.addValueChangeListener(e -> update.run());
            update.run();
            d.add(new VerticalLayout(
                    new Paragraph("Inicial: " + MONEY.format(resumen.montoInicial())),
                    new Paragraph("Ventas en efectivo: " + MONEY.format(resumen.totalVentas())),
                    new Paragraph("Ingresos: " + MONEY.format(resumen.totalIngresos())),
                    new Paragraph("Retiros: " + MONEY.format(resumen.totalRetiros())),
                    new Paragraph("Anulaciones: " + MONEY.format(resumen.totalAnulaciones())),
                    new Paragraph("Esperado: " + MONEY.format(resumen.esperado())),
                    counted,
                    preview
            ));
            Button confirm = new Button("Confirmar cierre", e -> {
                try {
                    CierreCajaResumenDTO closed = service.cerrar(c.id(), counted.getValue(), userId());
                    d.close();
                    refresh();
                    ViewSupport.success("Caja cerrada. Diferencia: " + MONEY.format(closed.diferencia()));
                } catch (RuntimeException ex) {
                    ViewSupport.error(ex);
                }
            });
            confirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            d.getFooter().add(new Button("Cancelar", e -> d.close()), confirm);
            d.open();
        }, () -> ViewSupport.error(new IllegalStateException("No hay caja abierta")));
    }

    private void amountDialog(String title, java.util.function.Consumer<BigDecimal> action) {
        Dialog d = new Dialog();
        d.setHeaderTitle(title);
        BigDecimalField amount = new BigDecimalField("Monto");
        Button save = new Button("Confirmar", e -> {
            try {
                action.accept(amount.getValue());
                d.close();
            } catch (RuntimeException ex) {
                ViewSupport.error(ex);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        d.add(amount);
        d.getFooter().add(new Button("Cancelar", e -> d.close()), save);
        d.open();
    }
}
