package com.example.demo.ui;

import com.example.demo.dto.ReglasOperativasDTO;
import com.example.demo.services.ReglasOperativasService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;

@Route(value = "reglas", layout = MainLayout.class)
@PageTitle("Reglas del negocio")
public class ReglasOperativasView extends VerticalLayout {
    public ReglasOperativasView(ReglasOperativasService service) {
        addClassName("content-view");
        setWidthFull();
        ReglasOperativasDTO current = service.obtener();

        Checkbox cash = check("Exigir una caja abierta para vender", "Evita ventas fuera de un turno de caja.", current.cajaObligatoria());
        Checkbox requireClient = check("Exigir un cliente en cada venta", "Útil para comercios que necesitan historial por comprador.", current.requerirClienteVenta());
        Checkbox negative = check("Permitir vender aunque no haya stock", "El inventario puede quedar negativo; recomendable sólo para rubros bajo pedido.", current.permitirVentaSinStock());
        Checkbox manualPrice = check("Permitir modificar el precio durante la venta", "Además requiere el permiso individual Modificar precios.", current.permitirPrecioManual());
        BigDecimalField maxDiscount = new BigDecimalField("Descuento máximo por venta (%)"); maxDiscount.setValue(current.descuentoMaximo());
        ComboBox<String> defaultPayment = new ComboBox<>("Medio de pago inicial");
        defaultPayment.setItems("EFECTIVO", "DEBITO", "CREDITO", "TRANSFERENCIA", "CUENTA_CORRIENTE");
        defaultPayment.setValue(current.medioPagoPredeterminado());
        Details sales = section("Ventas y caja", "Reglas que se aplican automáticamente en el punto de venta.",
                cash, requireClient, negative, manualPrice, form(maxDiscount, defaultPayment));
        sales.setOpened(true);

        Checkbox credit = check("Habilitar cuenta corriente o fiado", "Permite registrar ventas que el cliente pagará más adelante.", current.fiadoHabilitado());
        BigDecimalField defaultLimit = new BigDecimalField("Límite de crédito predeterminado"); defaultLimit.setValue(current.limiteCreditoPredeterminado());
        defaultLimit.setHelperText("Se usa cuando el cliente no tiene un límite propio.");
        defaultLimit.setEnabled(credit.getValue()); credit.addValueChangeListener(event -> defaultLimit.setEnabled(event.getValue()));
        Details clients = section("Clientes y crédito", "Define cómo se administran las deudas y límites de compra.", credit, defaultLimit);

        Checkbox returnsEnabled = check("Permitir devoluciones", "Habilita devoluciones parciales con reintegro o saldo a favor.", current.devolucionesHabilitadas());
        IntegerField returnDays = new IntegerField("Plazo máximo para devolver (días)"); returnDays.setValue(current.diasMaximosDevolucion());
        Checkbox ownerCancel = check("Sólo el dueño puede anular ventas completas", "Las devoluciones parciales siguen sus permisos específicos.", current.anulacionSoloDueno());
        returnDays.setEnabled(returnsEnabled.getValue()); returnsEnabled.addValueChangeListener(event -> returnDays.setEnabled(event.getValue()));
        Details afterSale = section("Devoluciones y anulaciones", "Controla correcciones posteriores a una venta confirmada.",
                returnsEnabled, returnDays, ownerCancel);

        Checkbox expiration = check("Controlar lotes y vencimientos", "Activar para alimentos, bebidas u otros productos con caducidad.", current.controlarVencimientos());
        Checkbox blockExpired = check("Bloquear la venta de mercadería vencida", "Si se desactiva, el sistema sólo mostrará la advertencia.", current.bloquearVentaVencidos());
        IntegerField expirationDays = new IntegerField("Avisar con anticipación (días)"); expirationDays.setValue(current.diasAlertaVencimiento());
        blockExpired.setEnabled(expiration.getValue()); expirationDays.setEnabled(expiration.getValue());
        expiration.addValueChangeListener(event -> { blockExpired.setEnabled(event.getValue()); expirationDays.setEnabled(event.getValue()); });
        Details inventory = section("Stock y vencimientos", "Permite adaptar la misma base a un kiosco o a un negocio sin productos perecederos.",
                expiration, blockExpired, expirationDays);

        Button save = new Button("Guardar reglas", event -> {
            try {
                service.guardar(new ReglasOperativasDTO(cash.getValue(), requireClient.getValue(), negative.getValue(),
                        maxDiscount.getValue(), manualPrice.getValue(), defaultPayment.getValue(), credit.getValue(),
                        defaultLimit.getValue() == null ? BigDecimal.ZERO : defaultLimit.getValue(), ownerCancel.getValue(),
                        returnsEnabled.getValue(), returnDays.getValue(), expiration.getValue(), blockExpired.getValue(),
                        expirationDays.getValue()), UserSession.getUser().id());
                ViewSupport.success("Reglas del negocio actualizadas");
            } catch (RuntimeException exception) {
                ViewSupport.error(exception);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(ViewSupport.header("Reglas del negocio"), new Paragraph("Estas opciones cambian el comportamiento diario sin requerir modificaciones de código."),
                sales, clients, afterSale, inventory, save);
    }

    private Checkbox check(String label, String helper, boolean value) {
        Checkbox checkbox = new Checkbox(label, value);
        checkbox.setHelperText(helper);
        return checkbox;
    }

    private Details section(String title, String description, com.vaadin.flow.component.Component... fields) {
        VerticalLayout content = new VerticalLayout(new Paragraph(description));
        content.add(fields); content.setPadding(false);
        Details details = new Details(title, content); details.setWidthFull();
        return details;
    }

    private FormLayout form(com.vaadin.flow.component.Component... fields) {
        FormLayout layout = new FormLayout(fields);
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("700px", 2));
        return layout;
    }
}
