package com.example.demo.ui;

import com.vaadin.flow.component.Key;
import com.example.demo.dto.ClienteDTO;
import com.example.demo.dto.ItemVentaCreateDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.dto.VentaCreateDTO;
import com.example.demo.dto.VentaDTO;
import com.example.demo.services.ClienteService;
import com.example.demo.services.ProductoService;
import com.example.demo.services.VentaService;
import com.example.demo.services.CajaService;
import com.example.demo.services.EmpleadoService;
import com.example.demo.services.ReglasOperativasService;
import com.example.demo.models.PermisoUsuario.Permiso;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route(value = "ventas", layout = MainLayout.class)
@PageTitle("Ventas | Franco")
public class VentasView extends VerticalLayout {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));
    private final VentaService service;
    private final ProductoService productoService;
    private final ClienteService clienteService;
    private final CajaService cajaService;
    private final ReglasOperativasService reglasService;
    private final boolean canDiscount;
    private final boolean canManualPrice;
    private final boolean canCancel;
    private final Grid<VentaDTO> grid = new Grid<>(VentaDTO.class, false);
    private final TextField search = new TextField();
    private final PaginationBar pagination = new PaginationBar(this::goToPage, this::changePageSize);
    private int currentPage;
    private int pageSize = 10;

    public VentasView(VentaService service, ProductoService productoService, ClienteService clienteService,
                      CajaService cajaService, ReglasOperativasService reglasService, EmpleadoService empleadoService) {
        this.service = service;
        this.productoService = productoService;
        this.clienteService = clienteService;
        this.cajaService = cajaService;
        this.reglasService = reglasService;
        this.canDiscount = UserSession.isAdmin() || empleadoService.tiene(UserSession.getUser().id(), Permiso.APLICAR_DESCUENTOS);
        var reglas = reglasService.obtener();
        this.canManualPrice = reglas.permitirPrecioManual()
                && (UserSession.isAdmin() || empleadoService.tiene(UserSession.getUser().id(), Permiso.MODIFICAR_PRECIOS));
        this.canCancel = UserSession.isSuperAdmin() || !reglas.anulacionSoloDueno()
                && (UserSession.isAdmin() || empleadoService.tiene(UserSession.getUser().id(), Permiso.ANULAR_VENTAS));
        addClassName("content-view"); setSizeFull();
        Button add = new Button("Nueva venta", VaadinIcon.PLUS.create(), event -> openForm());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        search.setPlaceholder("Buscar por venta, cliente, DNI o producto...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create()); search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(event -> resetAndRefresh());
        grid.addColumn(VentaDTO::id).setHeader("N°").setSortable(true);
        grid.addColumn(VentaDTO::fecha).setHeader("Fecha").setSortable(true);
        grid.addColumn(sale -> sale.cliente() == null ? "Sin cliente" : sale.cliente().nombre() + " " + sale.cliente().apellido())
                .setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(sale -> sale.items().stream().map(item -> item.nombreProducto() + " ×" + item.cantidadMostrada() + ("PESO".equals(item.unidadVenta()) ? " kg" : ""))
                .reduce((left, right) -> left + ", " + right).orElse("-")).setHeader("Productos").setFlexGrow(1);
        grid.addColumn(sale -> CURRENCY.format(sale.total())).setHeader("Total").setSortable(true);
        grid.addColumn(VentaDTO::medioPago).setHeader("Pago");
        grid.addColumn(VentaDTO::estado).setHeader("Estado");
        grid.addComponentColumn(this::saleActions).setHeader("Acciones");
        grid.setSizeFull();
        add(ViewSupport.header("Gestión de ventas", add), search, grid, pagination); expand(grid); refresh();
    }

    private void openForm() {
        Dialog dialog = new Dialog(); dialog.setHeaderTitle("Nueva venta"); dialog.setWidth("min(760px, 95vw)");
        ComboBox<ClienteDTO> client = new ComboBox<>("Cliente (opcional)");
        client.setItems(clienteService.listar()); client.setItemLabelGenerator(value -> value.nombre() + " " + value.apellido() + " · DNI " + value.dni());
        client.setClearButtonVisible(true); client.setWidthFull();
        TextField barcode = new TextField("Escanear código de barras"); barcode.setPlaceholder("Escaneá y presioná Enter"); barcode.setWidthFull();
        ComboBox<String> payment = new ComboBox<>("Medio de pago"); payment.setItems("EFECTIVO", "DEBITO", "CREDITO", "TRANSFERENCIA", "CUENTA_CORRIENTE"); payment.setValue("EFECTIVO");
        BigDecimalField received = new BigDecimalField("Monto recibido");
        BigDecimalField discount = new BigDecimalField("Descuento"); discount.setValue(BigDecimal.ZERO);
        discount.setReadOnly(!canDiscount);
        VerticalLayout itemContainer = new VerticalLayout(); itemContainer.setPadding(false);
        List<SaleItemRow> rows = new ArrayList<>();
        H3 total = new H3("Total: " + CURRENCY.format(BigDecimal.ZERO));
        Runnable updateTotal = () -> total.setText("Total: " + CURRENCY.format(rows.stream().map(SaleItemRow::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).subtract(discount.getValue() == null ? BigDecimal.ZERO : discount.getValue()).max(BigDecimal.ZERO)));
        Button addItem = new Button("Agregar producto", VaadinIcon.PLUS.create());
        addItem.addClickListener(event -> addItemRow(itemContainer, rows, updateTotal));
        barcode.addKeyPressListener(Key.ENTER, event -> productoService.buscarPorCodigo(barcode.getValue()).ifPresentOrElse(product -> {
            SaleItemRow existing = rows.stream().filter(r -> product.equals(r.product.getValue())).findFirst().orElse(null);
            if (existing != null) existing.quantity.setValue(existing.quantity.getValue().add(BigDecimal.ONE));
            else addItemRow(itemContainer, rows, updateTotal).product.setValue(product);
            barcode.clear(); updateTotal.run();
        }, () -> ViewSupport.error(new IllegalArgumentException("Código no encontrado"))));
        discount.addValueChangeListener(event -> updateTotal.run());
        dialog.add(new VerticalLayout(client, barcode, addItem, itemContainer, new HorizontalLayout(payment, received, discount), total));
        addItemRow(itemContainer, rows, updateTotal);
        Button cancel = new Button("Cancelar", event -> dialog.close());
        Button save = new Button("Crear venta", event -> {
            if (rows.isEmpty() || rows.stream().anyMatch(row -> row.product.getValue() == null || row.quantity.isEmpty())) {
                ViewSupport.error(new IllegalArgumentException("Seleccioná un producto y una cantidad en cada ítem")); return;
            }
            try {
                List<ItemVentaCreateDTO> items = rows.stream().map(row -> new ItemVentaCreateDTO(
                        row.product.getValue().id(), row.baseQuantity(), canManualPrice ? row.price.getValue() : null)).toList();
                var caja = cajaService.activa(UserSession.getUser().id());
                if (reglasService.obtener().cajaObligatoria() && caja.isEmpty())
                    throw new IllegalStateException("Abrí la caja antes de vender");
                var sale = service.crear(new VentaCreateDTO(client.getValue() == null ? null : client.getValue().id(), items,
                        payment.getValue(), received.getValue(), discount.getValue(), caja.map(com.example.demo.dto.CajaDTO::id).orElse(null), UserSession.getUser().id()));
                dialog.close(); refresh(); ViewSupport.success("Venta creada");
                getUI().ifPresent(ui -> ui.navigate("ticket/" + sale.getId()));
            } catch (RuntimeException exception) { ViewSupport.error(exception); }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY); dialog.getFooter().add(cancel, save); dialog.open();
    }

    private SaleItemRow addItemRow(VerticalLayout container, List<SaleItemRow> rows, Runnable updateTotal) {
        ComboBox<ProductoDTO> product = new ComboBox<>(); product.setPlaceholder("Producto");
        boolean allowWithoutStock = reglasService.obtener().permitirVentaSinStock();
        product.setItems(productoService.listar().stream().filter(value -> allowWithoutStock || value.stock() != null && value.stock() > 0).toList());
        product.setItemLabelGenerator(value -> value.nombre() + " · " + CURRENCY.format(value.precioVenta()) + " · stock " + value.stock());
        product.setWidthFull();
        BigDecimalField quantity = new BigDecimalField(); quantity.setPlaceholder("Cantidad / kg"); quantity.setValue(BigDecimal.ONE); quantity.setWidth("150px");
        BigDecimalField price = new BigDecimalField(); price.setPlaceholder("Precio"); price.setWidth("150px"); price.setReadOnly(!canManualPrice);
        Button remove = new Button(VaadinIcon.TRASH.create()); remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        HorizontalLayout layout = new HorizontalLayout(product, quantity, price, remove); layout.setWidthFull(); layout.expand(product);
        SaleItemRow row = new SaleItemRow(product, quantity, price, layout); rows.add(row); container.add(layout);
        product.addValueChangeListener(event -> { price.clear(); updateTotal.run(); });
        quantity.addValueChangeListener(event -> updateTotal.run()); price.addValueChangeListener(event -> updateTotal.run());
        remove.addClickListener(event -> { rows.remove(row); container.remove(layout); updateTotal.run(); });
        return row;
    }

    private HorizontalLayout saleActions(VentaDTO sale) {
        Button ticket = new Button(VaadinIcon.PRINT.create(), e -> ticket(sale));
        Button cancel = new Button(VaadinIcon.CLOSE.create(), e -> ViewSupport.confirm("¿Anular la venta " + sale.numeroComprobante() + "?", () -> {
            try { service.anular(sale.id()); refresh(); ViewSupport.success("Venta anulada y stock repuesto"); } catch (RuntimeException ex) { ViewSupport.error(ex); }
        }));
        cancel.setEnabled("CONFIRMADA".equals(sale.estado()) && canCancel);
        return new HorizontalLayout(ticket, cancel);
    }

    private void ticket(VentaDTO sale) {
        getUI().ifPresent(ui -> ui.getPage().open("ticket/" + sale.id(), "_blank"));
    }

    private void refresh() {
        String query = search.getValue().trim().toLowerCase();
        List<VentaDTO> data = service.listarDTO().stream().filter(sale -> {
            String client = sale.cliente() == null ? "" : sale.cliente().nombre() + " " + sale.cliente().apellido() + " " + sale.cliente().dni();
            String products = sale.items().stream().map(item -> item.nombreProducto()).reduce("", (left, right) -> left + " " + right);
            return query.isEmpty() || (sale.id() + " " + client + products).toLowerCase().contains(query);
        }).toList();
        int totalPages = Math.max(1, (int) Math.ceil((double) data.size() / pageSize));
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        int start = Math.min(currentPage * pageSize, data.size());
        int end = Math.min(start + pageSize, data.size());
        grid.setItems(data.subList(start, end));
        pagination.update(currentPage, totalPages, data.size());
    }

    private void resetAndRefresh() { currentPage = 0; refresh(); }

    private void goToPage(int page) { currentPage = Math.max(0, page); refresh(); }

    private void changePageSize(int size) { pageSize = size; resetAndRefresh(); }

    private record SaleItemRow(ComboBox<ProductoDTO> product, BigDecimalField quantity, BigDecimalField price, HorizontalLayout layout) {
        int baseQuantity() {
            if (product.getValue() == null || quantity.getValue() == null) throw new IllegalArgumentException("Cantidad incompleta");
            if (quantity.getValue().signum() <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
            try {
                return ("PESO".equals(product.getValue().unidadVenta()) ? quantity.getValue().movePointRight(3) : quantity.getValue()).intValueExact();
            } catch (ArithmeticException ex) { throw new IllegalArgumentException("Usá unidades enteras o hasta 3 decimales para el peso"); }
        }
        BigDecimal subtotal() {
            if (product.getValue() == null || quantity.getValue() == null) return BigDecimal.ZERO;
            ProductoDTO value = product.getValue();
            if (canUse(price)) return price.getValue().multiply(quantity.getValue());
            BigDecimal price = value.cantidadMinimaPromo() != null && value.precioPromocional() != null
                    && quantity.getValue().compareTo(BigDecimal.valueOf(value.cantidadMinimaPromo())) >= 0 ? value.precioPromocional() : value.precioVenta();
            return price.multiply(quantity.getValue());
        }
        private boolean canUse(BigDecimalField field) { return !field.isReadOnly() && field.getValue() != null; }
    }
}
