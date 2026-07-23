package com.example.demo.ui;

import com.example.demo.dto.*;
import com.example.demo.services.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.router.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

@Route(value="compras", layout=MainLayout.class) @PageTitle("Compras | Franco")
public class ComprasView extends VerticalLayout {
    private static final NumberFormat MONEY=NumberFormat.getCurrencyInstance(Locale.of("es","AR"));
    private final CompraService service; private final ProveedorService proveedores; private final ProductoService productos;
    private final ReglasOperativasService reglas;
    private final Grid<CompraDTO> grid=new Grid<>(CompraDTO.class,false);
    public ComprasView(CompraService service,ProveedorService proveedores,ProductoService productos,ReglasOperativasService reglas){
        this.service=service;this.proveedores=proveedores;this.productos=productos;this.reglas=reglas;
        addClassName("content-view");setSizeFull();Button add=new Button("Registrar compra",e->form());add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        grid.addColumn(CompraDTO::fecha).setHeader("Fecha");grid.addColumn(c->c.proveedor()==null?"—":c.proveedor().nombre()).setHeader("Proveedor");grid.addColumn(c->c.numeroComprobante()==null||c.numeroComprobante().isBlank()?"—":c.numeroComprobante()).setHeader("Comprobante");
        grid.addColumn(c->c.items().stream().map(i->i.producto()+" ×"+i.cantidad()+(i.codigoLote()==null?"":" · lote "+i.codigoLote())).reduce((a,b)->a+", "+b).orElse("-")).setHeader("Productos");
        grid.addColumn(c->MONEY.format(c.total())).setHeader("Total");grid.addColumn(CompraDTO::estado).setHeader("Estado");
        grid.addComponentColumn(c->{Button b=new Button("Anular",e->{try{service.anular(c.id());refresh();}catch(RuntimeException ex){ViewSupport.error(ex);}});b.setEnabled("RECIBIDA".equals(c.estado())&&UserSession.isSuperAdmin());return b;}).setHeader("Acciones");
        add(ViewSupport.header("Compras y reposición",add),grid);expand(grid);refresh();}
    private void refresh(){grid.setItems(service.listar());}
    private void form(){Dialog d=new Dialog();d.setHeaderTitle("Registrar compra");d.setWidth("min(850px,95vw)");
        boolean controlarVencimientos=reglas.obtener().controlarVencimientos();
        ComboBox<ProveedorDTO> supplier=new ComboBox<>("Proveedor (opcional)");
        supplier.setItems(proveedores.listar().stream().filter(ProveedorDTO::activo).toList());
        supplier.setItemLabelGenerator(ProveedorDTO::nombre);
        supplier.setWidthFull();
        supplier.setClearButtonVisible(true);
        supplier.setHelperText("Podés dejarlo vacío si solo estás reponiendo stock");
        TextField receipt=new TextField("N° comprobante (opcional)");
        receipt.setHelperText("Podés dejarlo vacío si no tenés factura o remito a mano");
        receipt.setClearButtonVisible(true);
        VerticalLayout rowsLayout=new VerticalLayout();List<Row> rows=new ArrayList<>();Button addItem=new Button("Agregar producto",e->row(rowsLayout,rows,controlarVencimientos));
        d.add(new VerticalLayout(supplier,receipt,addItem,rowsLayout));row(rowsLayout,rows,controlarVencimientos);Button save=new Button("Guardar y reponer stock",e->{try{
            if(rows.stream().anyMatch(r->r.product.getValue()==null||r.quantity.getValue()==null||r.cost.getValue()==null))throw new IllegalArgumentException("Completá los productos de la compra");
            String comprobante=receipt.getValue()==null||receipt.getValue().isBlank()?null:receipt.getValue().trim();
            service.crear(new CompraCreateDTO(supplier.getValue()==null?null:supplier.getValue().id(),comprobante,UserSession.getUser().id(),rows.stream().map(r->new ItemCompraCreateDTO(r.product.getValue().id(),r.baseQuantity(),r.cost.getValue(),r.expiration.getValue(),r.lot.getValue())).toList()));d.close();refresh();ViewSupport.success("Compra registrada y stock actualizado");
        }catch(RuntimeException ex){ViewSupport.error(ex);}});save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);d.getFooter().add(new Button("Cancelar",e->d.close()),save);d.open();}
    private void row(VerticalLayout layout,List<Row> rows,boolean controlarVencimientos){ComboBox<ProductoDTO> product=new ComboBox<>();product.setItems(productos.listar());product.setItemLabelGenerator(ProductoDTO::nombre);product.setPlaceholder("Producto");product.setWidthFull();
        BigDecimalField quantity=new BigDecimalField();quantity.setValue(BigDecimal.ONE);quantity.setPlaceholder("Cantidad / kg");BigDecimalField cost=new BigDecimalField();cost.setPlaceholder("Costo unitario");TextField lot=new TextField();lot.setPlaceholder("Lote");DatePicker expiration=new DatePicker();expiration.setPlaceholder("Vencimiento");Button remove=new Button("Quitar");
        lot.setVisible(controlarVencimientos);expiration.setVisible(controlarVencimientos);
        HorizontalLayout line=new HorizontalLayout(product,quantity,cost,lot,expiration,remove);line.addClassName("form-row");line.setWidthFull();line.expand(product);Row row=new Row(product,quantity,cost,lot,expiration,line);rows.add(row);layout.add(line);product.addValueChangeListener(e->{if(e.getValue()!=null)cost.setValue(e.getValue().costo());});remove.addClickListener(e->{rows.remove(row);layout.remove(line);});}
    private record Row(ComboBox<ProductoDTO> product,BigDecimalField quantity,BigDecimalField cost,TextField lot,DatePicker expiration,HorizontalLayout layout){
        int baseQuantity(){if(quantity.getValue()==null||quantity.getValue().signum()<=0)throw new IllegalArgumentException("La cantidad debe ser mayor a cero");try{return ("PESO".equals(product.getValue().unidadVenta())?quantity.getValue().movePointRight(3):quantity.getValue()).intValueExact();}catch(ArithmeticException ex){throw new IllegalArgumentException("Usá unidades enteras o hasta 3 decimales para el peso");}}
    }
}
