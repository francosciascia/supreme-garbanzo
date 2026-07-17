package com.example.demo.ui;

import com.example.demo.dto.ClienteDTO;
import com.example.demo.dto.ItemVentaCreateDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.dto.VentaCreateDTO;
import com.example.demo.dto.VentaDTO;
import com.example.demo.services.ClienteService;
import com.example.demo.services.ProductoService;
import com.example.demo.services.VentaService;
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
    private final Grid<VentaDTO> grid = new Grid<>(VentaDTO.class, false);
    private final TextField search = new TextField();
    private final PaginationBar pagination = new PaginationBar(this::goToPage, this::changePageSize);
    private int currentPage;
    private int pageSize = 10;

    public VentasView(VentaService service, ProductoService productoService, ClienteService clienteService) {
        this.service = service;
        this.productoService = productoService;
        this.clienteService = clienteService;
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
        grid.addColumn(sale -> sale.items().stream().map(item -> item.nombreProducto() + " ×" + item.cantidad())
                .reduce((left, right) -> left + ", " + right).orElse("-")).setHeader("Productos").setFlexGrow(1);
        grid.addColumn(sale -> CURRENCY.format(sale.total())).setHeader("Total").setSortable(true);
        grid.setSizeFull();
        add(ViewSupport.header("Gestión de ventas", add), search, grid, pagination); expand(grid); refresh();
    }

    private void openForm() {
        Dialog dialog = new Dialog(); dialog.setHeaderTitle("Nueva venta"); dialog.setWidth("min(760px, 95vw)");
        ComboBox<ClienteDTO> client = new ComboBox<>("Cliente (opcional)");
        client.setItems(clienteService.listar()); client.setItemLabelGenerator(value -> value.nombre() + " " + value.apellido() + " · DNI " + value.dni());
        client.setClearButtonVisible(true); client.setWidthFull();
        VerticalLayout itemContainer = new VerticalLayout(); itemContainer.setPadding(false);
        List<SaleItemRow> rows = new ArrayList<>();
        H3 total = new H3("Total: " + CURRENCY.format(BigDecimal.ZERO));
        Runnable updateTotal = () -> total.setText("Total: " + CURRENCY.format(rows.stream().map(SaleItemRow::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
        Button addItem = new Button("Agregar producto", VaadinIcon.PLUS.create());
        addItem.addClickListener(event -> addItemRow(itemContainer, rows, updateTotal));
        dialog.add(new VerticalLayout(client, addItem, itemContainer, total));
        addItemRow(itemContainer, rows, updateTotal);
        Button cancel = new Button("Cancelar", event -> dialog.close());
        Button save = new Button("Crear venta", event -> {
            if (rows.isEmpty() || rows.stream().anyMatch(row -> row.product.getValue() == null || row.quantity.isEmpty())) {
                ViewSupport.error(new IllegalArgumentException("Seleccioná un producto y una cantidad en cada ítem")); return;
            }
            try {
                List<ItemVentaCreateDTO> items = rows.stream().map(row -> new ItemVentaCreateDTO(row.product.getValue().id(), row.quantity.getValue())).toList();
                service.crear(new VentaCreateDTO(client.getValue() == null ? null : client.getValue().id(), items));
                dialog.close(); refresh(); ViewSupport.success("Venta creada");
            } catch (RuntimeException exception) { ViewSupport.error(exception); }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY); dialog.getFooter().add(cancel, save); dialog.open();
    }

    private void addItemRow(VerticalLayout container, List<SaleItemRow> rows, Runnable updateTotal) {
        ComboBox<ProductoDTO> product = new ComboBox<>(); product.setPlaceholder("Producto");
        product.setItems(productoService.listar().stream().filter(value -> value.stock() != null && value.stock() > 0).toList());
        product.setItemLabelGenerator(value -> value.nombre() + " · " + CURRENCY.format(value.precioVenta()) + " · stock " + value.stock());
        product.setWidthFull();
        IntegerField quantity = new IntegerField(); quantity.setPlaceholder("Cantidad"); quantity.setMin(1); quantity.setValue(1); quantity.setWidth("130px");
        Button remove = new Button(VaadinIcon.TRASH.create()); remove.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        HorizontalLayout layout = new HorizontalLayout(product, quantity, remove); layout.setWidthFull(); layout.expand(product);
        SaleItemRow row = new SaleItemRow(product, quantity, layout); rows.add(row); container.add(layout);
        product.addValueChangeListener(event -> updateTotal.run()); quantity.addValueChangeListener(event -> updateTotal.run());
        remove.addClickListener(event -> { rows.remove(row); container.remove(layout); updateTotal.run(); });
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

    private record SaleItemRow(ComboBox<ProductoDTO> product, IntegerField quantity, HorizontalLayout layout) {
        BigDecimal subtotal() {
            return product.getValue() == null || quantity.getValue() == null ? BigDecimal.ZERO :
                    product.getValue().precioVenta().multiply(BigDecimal.valueOf(quantity.getValue()));
        }
    }
}
