package com.example.demo.ui;

import com.example.demo.dto.ProveedorDTO;
import com.example.demo.services.ProveedorService;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;

@Route(value="proveedores", layout=MainLayout.class) @PageTitle("Proveedores | Franco")
public class ProveedoresView extends VerticalLayout {
    private final ProveedorService service; private final Grid<ProveedorDTO> grid = new Grid<>(ProveedorDTO.class, false);
    public ProveedoresView(ProveedorService service) { this.service=service; addClassName("content-view"); setSizeFull();
        Button add = new Button("Nuevo proveedor", e -> form(null)); add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        grid.addColumn(ProveedorDTO::nombre).setHeader("Nombre"); grid.addColumn(ProveedorDTO::cuit).setHeader("CUIT");
        grid.addColumn(ProveedorDTO::telefono).setHeader("Teléfono"); grid.addColumn(ProveedorDTO::email).setHeader("Email");
        grid.addComponentColumn(p -> new Button("Editar", e -> form(p))).setHeader("Acciones"); add(ViewSupport.header("Proveedores", add), grid); expand(grid); refresh(); }
    private void refresh(){ grid.setItems(service.listar()); }
    private void form(ProveedorDTO p) { Dialog d=new Dialog(); d.setHeaderTitle(p==null?"Nuevo proveedor":"Editar proveedor");
        TextField name=new TextField("Nombre"), cuit=new TextField("CUIT"), phone=new TextField("Teléfono"), email=new TextField("Email"), address=new TextField("Dirección"); Checkbox active=new Checkbox("Activo", true);
        if(p!=null){name.setValue(p.nombre()); set(cuit,p.cuit()); set(phone,p.telefono()); set(email,p.email()); set(address,p.direccion()); active.setValue(p.activo());}
        d.add(new VerticalLayout(name,cuit,phone,email,address,active)); Button save=new Button("Guardar",e->{try{service.guardar(p==null?null:p.id(),new ProveedorDTO(p==null?null:p.id(),name.getValue(),cuit.getValue(),phone.getValue(),email.getValue(),address.getValue(),active.getValue()));d.close();refresh();}catch(RuntimeException ex){ViewSupport.error(ex);}});
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY); d.getFooter().add(new Button("Cancelar",e->d.close()),save); d.open(); }
    private void set(TextField f,String v){f.setValue(v==null?"":v);}
}
