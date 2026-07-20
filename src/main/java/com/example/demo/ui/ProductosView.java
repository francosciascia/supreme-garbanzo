package com.example.demo.ui;

import com.example.demo.dto.CategoriaDTO;
import com.example.demo.dto.ProductoCUDTO;
import com.example.demo.dto.ProductoDTO;
import com.example.demo.services.CategoriaService;
import com.example.demo.services.ProductoService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.text.NumberFormat;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Route(value = "productos", layout = MainLayout.class)
@PageTitle("Productos | Franco")
public class ProductosView extends VerticalLayout {
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(Locale.of("es", "AR"));
    private final ProductoService service;
    private final CategoriaService categoriaService;
    private final Grid<ProductoDTO> grid = new Grid<>(ProductoDTO.class, false);
    private final TextField search = new TextField();
    private final ComboBox<CategoriaDTO> categoryFilter = new ComboBox<>();
    private final PaginationBar pagination = new PaginationBar(this::goToPage, this::changePageSize);
    private int currentPage;
    private int pageSize = 10;

    public ProductosView(ProductoService service, CategoriaService categoriaService) {
        this.service = service;
        this.categoriaService = categoriaService;
        addClassName("content-view");
        setSizeFull();
        Button add = new Button("Nuevo producto", VaadinIcon.PLUS.create(), event -> openForm(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.setVisible(UserSession.isAdmin());
        Button prices = new Button("Actualizar precios", event -> openPriceUpdate());
        prices.setVisible(UserSession.isAdmin());
        search.setPlaceholder("Buscar producto...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(event -> resetAndRefresh());
        categoryFilter.setPlaceholder("Todas las categorías");
        categoryFilter.setItemLabelGenerator(CategoriaDTO::nombre);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.setItems(categoriaService.listar());
        categoryFilter.addValueChangeListener(event -> resetAndRefresh());
        grid.addColumn(ProductoDTO::nombre).setHeader("Nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(product -> product.codigoBarras() == null ? "-" : product.codigoBarras()).setHeader("Código").setAutoWidth(true);
        grid.addColumn(product -> product.marca() == null ? "-" : product.marca()).setHeader("Marca").setAutoWidth(true);
        grid.addColumn(product -> product.categoria() == null ? "Sin categoría" : product.categoria().nombre()).setHeader("Categoría").setAutoWidth(true);
        grid.addColumn(product -> formatStock(product.stock(), product.unidadVenta()) + (product.stock() <= product.stockMinimo() ? " · BAJO" : "")).setHeader("Stock").setSortable(true);
        grid.addColumn(product -> CURRENCY.format(product.costo())).setHeader("Costo");
        grid.addColumn(product -> CURRENCY.format(product.precioVenta())).setHeader("Precio venta");
        grid.addColumn(product -> Boolean.TRUE.equals(product.vencimiento()) ? "Sí" : "No").setHeader("Vence");
        grid.addComponentColumn(this::actions).setHeader("Acciones").setAutoWidth(true);
        grid.setSizeFull();
        add(ViewSupport.header("Gestión de productos", prices, add), new HorizontalLayout(search, categoryFilter), grid, pagination);
        expand(grid);
        refresh();
    }

    private HorizontalLayout actions(ProductoDTO product) {
        Button edit = new Button(VaadinIcon.EDIT.create(), event -> openForm(product));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        edit.setVisible(UserSession.isAdmin());
        Button delete = new Button(VaadinIcon.TRASH.create(), event -> ViewSupport.confirm(
                "¿Eliminar el producto " + product.nombre() + "?", () -> delete(product.id())));
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        delete.setVisible(UserSession.isSuperAdmin());
        return new HorizontalLayout(edit, delete);
    }

    private void openForm(ProductoDTO product) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(product == null ? "Nuevo producto" : "Editar producto");
        TextField name = new TextField("Nombre");
        TextArea description = new TextArea("Descripción");
        TextField barcode = new TextField("Código de barras");
        TextField brand = new TextField("Marca");
        BigDecimalField stock = new BigDecimalField("Stock (unidades o kg)");
        BigDecimalField minimumStock = new BigDecimalField("Stock mínimo"); minimumStock.setValue(BigDecimal.valueOf(5));
        ComboBox<String> unit = new ComboBox<>("Venta por"); unit.setItems("UNIDAD", "PESO"); unit.setValue("UNIDAD");
        BigDecimalField cost = new BigDecimalField("Costo");
        BigDecimalField salePrice = new BigDecimalField("Precio de venta");
        IntegerField promoQuantity = new IntegerField("Promo desde unidades"); promoQuantity.setMin(2);
        BigDecimalField promoPrice = new BigDecimalField("Precio unitario promocional");
        ComboBox<CategoriaDTO> category = new ComboBox<>("Categoría");
        category.setItems(categoriaService.listar()); category.setItemLabelGenerator(CategoriaDTO::nombre);
        Checkbox expiration = new Checkbox("Producto con vencimiento");
        DatePicker expirationDate = new DatePicker("Fecha de vencimiento");
        if (product != null) {
            name.setValue(product.nombre()); description.setValue(product.descripcion() == null ? "" : product.descripcion());
            stock.setValue(displayStock(product.stock(), product.unidadVenta())); cost.setValue(product.costo()); salePrice.setValue(product.precioVenta());
            category.setValue(product.categoria()); expiration.setValue(Boolean.TRUE.equals(product.vencimiento()));
            barcode.setValue(product.codigoBarras() == null ? "" : product.codigoBarras());
            brand.setValue(product.marca() == null ? "" : product.marca()); minimumStock.setValue(displayStock(product.stockMinimo(), product.unidadVenta()));
            unit.setValue(product.unidadVenta()); expirationDate.setValue(product.fechaVencimiento());
            promoQuantity.setValue(product.cantidadMinimaPromo()); promoPrice.setValue(product.precioPromocional());
        }
        VerticalLayout form = new VerticalLayout(name, new HorizontalLayout(barcode, brand), description,
                new HorizontalLayout(stock, minimumStock, unit), new HorizontalLayout(cost, salePrice),
                new HorizontalLayout(promoQuantity, promoPrice),
                new HorizontalLayout(expiration, expirationDate), category);
        form.getChildren().filter(component -> component instanceof com.vaadin.flow.component.HasSize)
                .forEach(component -> ((com.vaadin.flow.component.HasSize) component).setWidthFull());
        dialog.add(form);
        Button cancel = new Button("Cancelar", event -> dialog.close());
        Button save = new Button("Guardar", event -> {
            if (name.isEmpty() || stock.isEmpty() || cost.isEmpty() || salePrice.isEmpty()) {
                ViewSupport.error(new IllegalArgumentException("Completá todos los campos obligatorios")); return;
            }
            try {
                ProductoCUDTO dto = new ProductoCUDTO(name.getValue(), description.getValue(), baseStock(stock.getValue(), unit.getValue()),
                        expiration.getValue(), cost.getValue(), salePrice.getValue(),
                        category.getValue() == null ? null : category.getValue().id(), barcode.getValue(), brand.getValue(),
                        baseStock(minimumStock.getValue(), unit.getValue()), unit.getValue(), expiration.getValue() ? expirationDate.getValue() : null,
                        promoQuantity.getValue(), promoPrice.getValue());
                if (product == null) service.crear(dto); else service.actualizar(product.id(), dto);
                dialog.close(); refresh(); ViewSupport.success("Producto guardado");
            } catch (RuntimeException exception) { ViewSupport.error(exception); }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void delete(Long id) {
        try { service.eliminar(id); refresh(); ViewSupport.success("Producto eliminado"); }
        catch (RuntimeException exception) { ViewSupport.error(exception); }
    }

    private void openPriceUpdate() {
        Dialog dialog = new Dialog(); dialog.setHeaderTitle("Actualización masiva de precios");
        BigDecimalField percentage = new BigDecimalField("Porcentaje (ej. 10 o -5)");
        ComboBox<CategoriaDTO> category = new ComboBox<>("Categoría (opcional)"); category.setItems(categoriaService.listar());
        category.setItemLabelGenerator(CategoriaDTO::nombre); category.setClearButtonVisible(true); dialog.add(new VerticalLayout(percentage, category));
        Button save = new Button("Aplicar", event -> { try { int count = service.actualizarPrecios(percentage.getValue(), category.getValue() == null ? null : category.getValue().id());
            dialog.close(); refresh(); ViewSupport.success("Se actualizaron " + count + " productos"); } catch (RuntimeException ex) { ViewSupport.error(ex); } });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY); dialog.getFooter().add(new Button("Cancelar", event -> dialog.close()), save); dialog.open();
    }

    private void refresh() {
        CategoriaDTO selected = categoryFilter.getValue();
        Page<ProductoDTO> data = service.buscar(search.getValue(), selected == null ? null : selected.id(),
                PageRequest.of(currentPage, pageSize, Sort.by("nombre").ascending()));
        if (data.getTotalPages() > 0 && currentPage >= data.getTotalPages()) {
            currentPage = data.getTotalPages() - 1;
            refresh();
            return;
        }
        grid.setItems(data.getContent());
        pagination.update(currentPage, data.getTotalPages(), data.getTotalElements());
    }

    private void resetAndRefresh() { currentPage = 0; refresh(); }

    private String formatStock(Integer stock, String unit) { return "PESO".equals(unit) ? displayStock(stock, unit) + " kg" : String.valueOf(stock); }
    private BigDecimal displayStock(Integer stock, String unit) { return "PESO".equals(unit) ? BigDecimal.valueOf(stock).movePointLeft(3) : BigDecimal.valueOf(stock); }
    private int baseStock(BigDecimal stock, String unit) { if(stock==null||stock.signum()<0)throw new IllegalArgumentException("El stock no puede ser negativo");try { return ("PESO".equals(unit) ? stock.movePointRight(3) : stock).intValueExact(); }
        catch (ArithmeticException ex) { throw new IllegalArgumentException("Usá unidades enteras o hasta 3 decimales para el peso"); } }

    private void goToPage(int page) { currentPage = Math.max(0, page); refresh(); }

    private void changePageSize(int size) { pageSize = size; resetAndRefresh(); }
}
