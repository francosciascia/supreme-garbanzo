package com.example.demo.ui;

import com.example.demo.dto.VentaDTO;
import com.example.demo.models.PermisoUsuario.Permiso;
import com.example.demo.services.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.text.NumberFormat;
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
        addClassName("content-view");
        setSizeFull();
        Button add = new Button("Nueva venta", VaadinIcon.PLUS.create(), event -> openForm());
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button pos = new Button("POS pantalla completa", VaadinIcon.EXPAND_FULL.create(),
                event -> getUI().ifPresent(ui -> ui.navigate(PosView.class)));
        search.setPlaceholder("Buscar por venta, cliente, DNI o producto...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(event -> resetAndRefresh());
        grid.addColumn(VentaDTO::id).setHeader("N°").setSortable(true);
        grid.addColumn(VentaDTO::fecha).setHeader("Fecha").setSortable(true);
        grid.addColumn(sale -> sale.cliente() == null ? "Sin cliente" : sale.cliente().nombre() + " " + sale.cliente().apellido())
                .setHeader("Cliente").setAutoWidth(true);
        grid.addColumn(sale -> sale.items().stream()
                .map(item -> item.nombreProducto() + " ×" + item.cantidadMostrada() + ("PESO".equals(item.unidadVenta()) ? " kg" : ""))
                .reduce((left, right) -> left + ", " + right).orElse("-")).setHeader("Productos").setFlexGrow(1);
        grid.addColumn(sale -> CURRENCY.format(sale.total())).setHeader("Total").setSortable(true);
        grid.addColumn(VentaDTO::medioPago).setHeader("Pago");
        grid.addColumn(VentaDTO::estado).setHeader("Estado");
        grid.addComponentColumn(this::saleActions).setHeader("Acciones");
        grid.setSizeFull();
        add(ViewSupport.header("Gestión de ventas", pos, add), search, grid, pagination);
        expand(grid);
        refresh();
    }

    private void openForm() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Nueva venta");
        dialog.setWidth("min(760px, 95vw)");
        dialog.add(new NuevaVentaPanel(service, productoService, clienteService, cajaService, reglasService,
                canDiscount, canManualPrice, sale -> refresh(), dialog::close));
        dialog.open();
    }

    private HorizontalLayout saleActions(VentaDTO sale) {
        Button ticket = new Button(VaadinIcon.PRINT.create(), e -> ticket(sale));
        Button cancel = new Button(VaadinIcon.CLOSE.create(), e -> anular(sale));
        cancel.setEnabled("CONFIRMADA".equals(sale.estado()) && canCancel);
        return new HorizontalLayout(ticket, cancel);
    }

    private void anular(VentaDTO sale) {
        boolean requireReason = reglasService.obtener().motivoAnulacionObligatorio();
        if (!requireReason) {
            ViewSupport.confirm("¿Anular la venta " + sale.numeroComprobante() + "?", () -> doAnular(sale.id(), null));
            return;
        }
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Anular venta");
        TextArea reason = new TextArea("Motivo");
        reason.setWidthFull();
        Button accept = new Button("Anular", e -> {
            try {
                doAnular(sale.id(), reason.getValue());
                dialog.close();
            } catch (RuntimeException ex) {
                ViewSupport.error(ex);
            }
        });
        accept.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        dialog.add(reason);
        dialog.getFooter().add(new Button("Cancelar", e -> dialog.close()), accept);
        dialog.open();
    }

    private void doAnular(Long id, String motivo) {
        try {
            service.anular(id, motivo);
            refresh();
            ViewSupport.success("Venta anulada y stock repuesto");
        } catch (RuntimeException ex) {
            ViewSupport.error(ex);
        }
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

    private void resetAndRefresh() {
        currentPage = 0;
        refresh();
    }

    private void goToPage(int page) {
        currentPage = Math.max(0, page);
        refresh();
    }

    private void changePageSize(int size) {
        pageSize = size;
        resetAndRefresh();
    }
}
