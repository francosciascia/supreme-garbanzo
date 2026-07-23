package com.example.demo.ui;

import com.example.demo.dto.*;
import com.example.demo.services.*;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/** Formulario de cobro: buscás un producto y se suma solo al carrito. */
final class NuevaVentaPanel extends VerticalLayout {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));

    NuevaVentaPanel(VentaService service, ProductoService productoService, ClienteService clienteService,
                    CajaService cajaService, ReglasOperativasService reglasService,
                    boolean canDiscount, boolean canManualPrice,
                    Consumer<VentaDTO> onCreated, Runnable onCancel) {
        this(service, productoService, clienteService, cajaService, reglasService,
                canDiscount, canManualPrice, false, onCreated, onCancel);
    }

    NuevaVentaPanel(VentaService service, ProductoService productoService, ClienteService clienteService,
                    CajaService cajaService, ReglasOperativasService reglasService,
                    boolean canDiscount, boolean canManualPrice, boolean posMode,
                    Consumer<VentaDTO> onCreated, Runnable onCancel) {
        setPadding(false);
        setSpacing(false);
        setWidthFull();
        addClassName(posMode ? "pos-panel" : "sale-panel");

        var reglas = reglasService.obtener();
        boolean allowWithoutStock = reglas.permitirVentaSinStock();
        List<ProductoDTO> catalog = productoService.listar().stream()
                .filter(value -> allowWithoutStock || value.stock() != null && value.stock() > 0)
                .toList();

        ComboBox<ClienteDTO> client = new ComboBox<>(reglas.requerirClienteVenta() ? "Cliente" : "Cliente (opcional)");
        client.setItems(clienteService.listar());
        client.setItemLabelGenerator(value -> value.nombre() + " " + value.apellido() + " · DNI " + value.dni());
        client.setClearButtonVisible(!reglas.requerirClienteVenta());
        client.setWidthFull();
        if (reglas.requerirClienteVenta()) client.setRequired(true);

        ComboBox<String> payment = new ComboBox<>("Medio de pago");
        List<String> medios = new ArrayList<>(List.of("EFECTIVO", "DEBITO", "CREDITO", "TRANSFERENCIA"));
        if (reglas.fiadoHabilitado()) medios.add("CUENTA_CORRIENTE");
        payment.setItems(medios);
        String defaultPayment = medios.contains(reglas.medioPagoPredeterminado())
                ? reglas.medioPagoPredeterminado() : "EFECTIVO";
        payment.setValue(defaultPayment);
        payment.setWidthFull();

        BigDecimalField received = new BigDecimalField("Monto recibido");
        received.setWidthFull();
        received.setVisible("EFECTIVO".equals(defaultPayment));

        BigDecimalField discount = new BigDecimalField("Descuento");
        discount.setValue(BigDecimal.ZERO);
        discount.setReadOnly(!canDiscount);
        discount.setWidthFull();

        Span change = new Span("");
        change.addClassName("pos-change");
        H2 total = new H2(CURRENCY.format(BigDecimal.ZERO));
        total.addClassName("pos-total");
        Span itemsCount = new Span("0 ítems");
        itemsCount.addClassName("pos-items-count");

        VerticalLayout itemContainer = new VerticalLayout();
        itemContainer.setPadding(false);
        itemContainer.setSpacing(true);
        itemContainer.addClassName("pos-cart");
        List<CartLine> lines = new ArrayList<>();
        Paragraph emptyCart = new Paragraph("Todavía no hay productos. Buscá uno y se agrega solo.");
        emptyCart.addClassName("pos-empty-cart");
        itemContainer.add(emptyCart);

        Runnable updateTotal = () -> {
            itemsCount.setText(lines.size() + (lines.size() == 1 ? " ítem" : " ítems"));
            emptyCart.setVisible(lines.isEmpty());
            BigDecimal subtotal = lines.stream().map(CartLine::subtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .subtract(discount.getValue() == null ? BigDecimal.ZERO : discount.getValue())
                    .max(BigDecimal.ZERO);
            if ("EFECTIVO".equals(payment.getValue()) && reglas.redondeoEfectivo()) {
                subtotal = subtotal.setScale(0, java.math.RoundingMode.HALF_UP);
            }
            total.setText(CURRENCY.format(subtotal));
            received.setVisible("EFECTIVO".equals(payment.getValue()));
            if ("EFECTIVO".equals(payment.getValue()) && received.getValue() != null) {
                BigDecimal vuelto = received.getValue().subtract(subtotal);
                change.setText(vuelto.signum() < 0
                        ? "Falta " + CURRENCY.format(vuelto.abs())
                        : "Vuelto " + CURRENCY.format(vuelto));
                change.getElement().getClassList().set("pos-change-warn", vuelto.signum() < 0);
                change.getElement().getClassList().set("pos-change-ok", vuelto.signum() >= 0);
            } else {
                change.setText("");
                change.getElement().getClassList().remove("pos-change-warn");
                change.getElement().getClassList().remove("pos-change-ok");
            }
        };

        Consumer<ProductoDTO> addOrIncrement = product -> {
            if (product == null) return;
            CartLine existing = lines.stream()
                    .filter(l -> Objects.equals(l.product.id(), product.id()))
                    .findFirst().orElse(null);
            BigDecimal step = "PESO".equals(product.unidadVenta()) ? new BigDecimal("0.100") : BigDecimal.ONE;
            if (existing != null) {
                existing.quantity.setValue(existing.quantity.getValue() == null
                        ? step : existing.quantity.getValue().add(step));
            } else {
                addLine(itemContainer, lines, product, updateTotal, canManualPrice);
            }
            updateTotal.run();
        };

        ComboBox<ProductoDTO> search = new ComboBox<>("Buscar producto");
        search.setPlaceholder("Escribí el nombre y elegilo — se suma solo");
        search.setItems(catalog);
        search.setItemLabelGenerator(p -> p.nombre() + " · " + CURRENCY.format(p.precioVenta())
                + (p.codigoBarras() == null || p.codigoBarras().isBlank() ? "" : " · " + p.codigoBarras()));
        search.setClearButtonVisible(true);
        search.setWidthFull();
        search.addClassName("pos-search");
        search.setAutofocus(true);
        Runnable focusSearch = () -> getUI().ifPresent(ui -> ui.getPage().executeJs(
                "setTimeout(()=>$0.focus(),40)", search.getElement()));
        search.addValueChangeListener(event -> {
            if (!event.isFromClient() || event.getValue() == null) return;
            addOrIncrement.accept(event.getValue());
            search.clear();
            focusSearch.run();
        });

        TextField barcode = new TextField("Código de barras (opcional)");
        barcode.setPlaceholder("Si tenés lectora o el código, Enter suma otra unidad");
        barcode.setWidthFull();
        barcode.setClearButtonVisible(true);
        barcode.addKeyPressListener(Key.ENTER, event -> {
            String code = barcode.getValue() == null ? "" : barcode.getValue().trim();
            if (code.isEmpty()) return;
            productoService.buscarPorCodigo(code).ifPresentOrElse(product -> {
                if (!allowWithoutStock && (product.stock() == null || product.stock() <= 0)) {
                    ViewSupport.error(new IllegalStateException("Sin stock de " + product.nombre()));
                    return;
                }
                addOrIncrement.accept(product);
                barcode.clear();
                focusSearch.run();
            }, () -> ViewSupport.error(new IllegalArgumentException("Código no encontrado")));
        });

        discount.addValueChangeListener(event -> updateTotal.run());
        received.addValueChangeListener(event -> updateTotal.run());
        payment.addValueChangeListener(event -> updateTotal.run());

        Runnable clearCart = () -> {
            lines.clear();
            itemContainer.removeAll();
            itemContainer.add(emptyCart);
            client.clear();
            received.clear();
            discount.setValue(BigDecimal.ZERO);
            payment.setValue(defaultPayment);
            search.clear();
            barcode.clear();
            updateTotal.run();
            focusSearch.run();
        };

        Button clear = new Button(posMode ? "Limpiar" : "Cancelar",
                event -> {
                    if (posMode) clearCart.run();
                    else onCancel.run();
                });
        Button save = new Button(posMode ? "Cobrar" : "Crear venta", VaadinIcon.CHECK.create(), event -> {
            if (reglas.requerirClienteVenta() && client.getValue() == null) {
                ViewSupport.error(new IllegalArgumentException("Seleccioná un cliente"));
                return;
            }
            if (lines.isEmpty()) {
                ViewSupport.error(new IllegalArgumentException("Agregá al menos un producto"));
                return;
            }
            try {
                List<ItemVentaCreateDTO> items = lines.stream()
                        .map(line -> new ItemVentaCreateDTO(line.product.id(), line.baseQuantity(),
                                canManualPrice ? line.price.getValue() : null))
                        .toList();
                var caja = cajaService.activa(UserSession.getUser().id());
                if (reglas.cajaObligatoria() && caja.isEmpty())
                    throw new IllegalStateException("Abrí la caja antes de vender");
                var sale = service.crear(new VentaCreateDTO(
                        client.getValue() == null ? null : client.getValue().id(), items,
                        payment.getValue(), received.getValue(), discount.getValue(),
                        caja.map(CajaDTO::id).orElse(null), UserSession.getUser().id()));
                VentaDTO dto = service.detalleDTO(sale.getId()).orElseThrow();
                ViewSupport.success("Venta " + dto.numeroComprobante() + " · " + CURRENCY.format(dto.total()));
                if (reglas.imprimirTicketAuto()) {
                    getUI().ifPresent(ui -> ui.getPage().open("ticket/" + sale.getId(), "_blank"));
                }
                clearCart.run();
                onCreated.accept(dto);
            } catch (RuntimeException exception) {
                ViewSupport.error(exception);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        if (posMode) save.addThemeVariants(ButtonVariant.LUMO_LARGE);
        save.addClickShortcut(Key.ENTER, KeyModifier.CONTROL);
        save.setWidthFull();

        Paragraph tip = new Paragraph("Elegí un producto → entra al carrito. Si lo elegís de nuevo, suma cantidad. Ctrl+Enter cobra.");
        tip.addClassName("pos-tip");
        HorizontalLayout cartHeader = new HorizontalLayout(new Span("Carrito"), itemsCount);
        cartHeader.setWidthFull();
        cartHeader.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        cartHeader.addClassName("pos-cart-header");

        if (posMode) {
            VerticalLayout left = new VerticalLayout(tip, search, barcode, client, cartHeader, itemContainer);
            left.addClassName("pos-left");
            left.setPadding(false);
            left.setSpacing(true);
            left.setWidthFull();

            Span totalLabel = new Span("Total");
            totalLabel.addClassName("pos-total-label");
            Div totalBlock = new Div(totalLabel, total, change);
            totalBlock.addClassName("pos-total-block");
            VerticalLayout right = new VerticalLayout(totalBlock, payment, received, discount, save, clear);
            right.addClassName("pos-right");
            right.setPadding(false);
            right.setSpacing(true);

            Div grid = new Div(left, right);
            grid.addClassName("pos-grid");
            add(grid);
        } else {
            HorizontalLayout paymentRow = new HorizontalLayout(payment, received, discount);
            paymentRow.addClassName("form-row");
            paymentRow.setWidthFull();
            HorizontalLayout actions = new HorizontalLayout(clear, save);
            actions.addClassName("view-actions");
            Paragraph changeP = new Paragraph(change);
            add(tip, client, search, barcode, cartHeader, itemContainer, paymentRow, total, changeP, actions);
        }
        updateTotal.run();
        focusSearch.run();
    }

    private static void addLine(VerticalLayout container, List<CartLine> lines, ProductoDTO product,
                                Runnable updateTotal, boolean canManualPrice) {
        BigDecimal step = "PESO".equals(product.unidadVenta()) ? new BigDecimal("0.100") : BigDecimal.ONE;
        BigDecimalField quantity = new BigDecimalField();
        quantity.setValue(step);
        quantity.setWidth("88px");
        BigDecimalField price = new BigDecimalField();
        price.setValue(precioAplicable(product, step));
        price.setWidth("100px");
        price.setReadOnly(!canManualPrice);
        if (!canManualPrice) price.setVisible(false);

        Span name = new Span(product.nombre());
        name.addClassName("pos-line-name");
        Span lineTotal = new Span();
        lineTotal.addClassName("pos-line-total");

        Button minus = new Button(VaadinIcon.MINUS.create());
        Button plus = new Button(VaadinIcon.PLUS.create());
        minus.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        plus.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        Button remove = new Button(VaadinIcon.TRASH.create());
        remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout qtyBox = new HorizontalLayout(minus, quantity, plus);
        qtyBox.setAlignItems(HorizontalLayout.Alignment.CENTER);
        qtyBox.setSpacing(false);
        qtyBox.addClassName("pos-qty-box");

        HorizontalLayout layout = new HorizontalLayout(name, qtyBox, price, lineTotal, remove);
        layout.addClassName("sale-item-row");
        layout.addClassName("pos-cart-line");
        layout.setWidthFull();
        layout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        layout.expand(name);

        CartLine line = new CartLine(product, quantity, price, layout, lineTotal);
        lines.add(line);
        container.add(layout);

        Runnable refreshLine = () -> {
            lineTotal.setText(CURRENCY.format(line.subtotal()));
            updateTotal.run();
        };
        minus.addClickListener(e -> {
            BigDecimal current = quantity.getValue() == null ? step : quantity.getValue();
            BigDecimal next = current.subtract(step);
            if (next.signum() <= 0) {
                lines.remove(line);
                container.remove(layout);
                updateTotal.run();
            } else {
                quantity.setValue(next);
            }
        });
        plus.addClickListener(e -> {
            BigDecimal current = quantity.getValue() == null ? BigDecimal.ZERO : quantity.getValue();
            quantity.setValue(current.add(step));
        });
        quantity.addValueChangeListener(e -> {
            if (!canManualPrice && e.getValue() != null) price.setValue(precioAplicable(product, e.getValue()));
            refreshLine.run();
        });
        price.addValueChangeListener(e -> refreshLine.run());
        remove.addClickListener(e -> {
            lines.remove(line);
            container.remove(layout);
            updateTotal.run();
        });
        refreshLine.run();
    }

    private static BigDecimal precioAplicable(ProductoDTO product, BigDecimal cantidad) {
        if (product.cantidadMinimaPromo() != null && product.precioPromocional() != null
                && !"PESO".equals(product.unidadVenta())
                && cantidad != null
                && cantidad.compareTo(BigDecimal.valueOf(product.cantidadMinimaPromo())) >= 0) {
            return product.precioPromocional();
        }
        return product.precioVenta();
    }

    private static final class CartLine {
        private final ProductoDTO product;
        private final BigDecimalField quantity;
        private final BigDecimalField price;
        private final HorizontalLayout layout;
        private final Span lineTotal;

        private CartLine(ProductoDTO product, BigDecimalField quantity, BigDecimalField price,
                         HorizontalLayout layout, Span lineTotal) {
            this.product = product;
            this.quantity = quantity;
            this.price = price;
            this.layout = layout;
            this.lineTotal = lineTotal;
        }

        private int baseQuantity() {
            if (quantity.getValue() == null) throw new IllegalArgumentException("Cantidad incompleta");
            try {
                return ("PESO".equals(product.unidadVenta())
                        ? quantity.getValue().movePointRight(3) : quantity.getValue()).intValueExact();
            } catch (ArithmeticException ex) {
                throw new IllegalArgumentException("Usá unidades enteras o hasta 3 decimales para el peso");
            }
        }

        private BigDecimal subtotal() {
            if (quantity.getValue() == null) return BigDecimal.ZERO;
            BigDecimal unit = price.getValue() != null ? price.getValue() : precioAplicable(product, quantity.getValue());
            return unit.multiply(quantity.getValue());
        }
    }
}
