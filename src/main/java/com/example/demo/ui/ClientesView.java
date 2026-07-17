package com.example.demo.ui;

import com.example.demo.dto.ClienteCUDTO;
import com.example.demo.dto.ClienteDTO;
import com.example.demo.services.ClienteService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "clientes", layout = MainLayout.class)
@PageTitle("Clientes | Franco")
public class ClientesView extends VerticalLayout {
    private final ClienteService service;
    private final Grid<ClienteDTO> grid = new Grid<>(ClienteDTO.class, false);
    private final TextField search = new TextField();

    public ClientesView(ClienteService service) {
        this.service = service;
        addClassName("content-view"); setSizeFull();
        Button add = new Button("Nuevo cliente", VaadinIcon.PLUS.create(), event -> openForm(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY); add.setVisible(UserSession.isAdmin());
        search.setPlaceholder("Buscar por nombre, DNI o email..."); search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.LAZY); search.addValueChangeListener(event -> refresh());
        grid.addColumn(client -> client.nombre() + " " + client.apellido()).setHeader("Cliente").setSortable(true).setAutoWidth(true);
        grid.addColumn(ClienteDTO::dni).setHeader("DNI");
        grid.addColumn(ClienteDTO::email).setHeader("Email").setAutoWidth(true);
        grid.addColumn(ClienteDTO::telefono).setHeader("Teléfono");
        grid.addColumn(client -> client.activo() ? "Activo" : "Inactivo").setHeader("Estado");
        grid.addComponentColumn(this::actions).setHeader("Acciones").setAutoWidth(true);
        grid.setSizeFull();
        add(ViewSupport.header("Gestión de clientes", add), search, grid); expand(grid); refresh();
    }

    private HorizontalLayout actions(ClienteDTO client) {
        Button edit = new Button(VaadinIcon.EDIT.create(), event -> openForm(client));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY); edit.setVisible(UserSession.isAdmin());
        Button delete = new Button(VaadinIcon.TRASH.create(), event -> ViewSupport.confirm(
                "¿Eliminar a " + client.nombre() + " " + client.apellido() + "?", () -> delete(client.id())));
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR); delete.setVisible(UserSession.isSuperAdmin());
        return new HorizontalLayout(edit, delete);
    }

    private void openForm(ClienteDTO client) {
        Dialog dialog = new Dialog(); dialog.setHeaderTitle(client == null ? "Nuevo cliente" : "Editar cliente");
        TextField name = new TextField("Nombre"); TextField surname = new TextField("Apellido");
        IntegerField dni = new IntegerField("DNI"); EmailField email = new EmailField("Email");
        TextField phone = new TextField("Teléfono"); TextField address = new TextField("Dirección");
        Checkbox active = new Checkbox("Cliente activo", true);
        if (client != null) {
            name.setValue(client.nombre()); surname.setValue(client.apellido()); dni.setValue(client.dni());
            email.setValue(client.email() == null ? "" : client.email()); phone.setValue(client.telefono() == null ? "" : client.telefono());
            address.setValue(client.direccion() == null ? "" : client.direccion()); active.setValue(client.activo());
        }
        dialog.add(new VerticalLayout(new HorizontalLayout(name, surname), new HorizontalLayout(dni, email), phone, address, active));
        Button cancel = new Button("Cancelar", event -> dialog.close());
        Button save = new Button("Guardar", event -> {
            if (name.isEmpty() || surname.isEmpty() || dni.isEmpty()) { ViewSupport.error(new IllegalArgumentException("Nombre, apellido y DNI son obligatorios")); return; }
            try {
                ClienteCUDTO dto = new ClienteCUDTO(name.getValue(), surname.getValue(), dni.getValue(), email.getValue(), phone.getValue(), address.getValue(), active.getValue());
                if (client == null) service.crear(dto); else service.actualizar(client.id(), dto);
                dialog.close(); refresh(); ViewSupport.success("Cliente guardado");
            } catch (RuntimeException exception) { ViewSupport.error(exception); }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY); dialog.getFooter().add(cancel, save); dialog.open();
    }

    private void delete(Long id) {
        try { service.eliminar(id); refresh(); ViewSupport.success("Cliente eliminado"); }
        catch (RuntimeException exception) { ViewSupport.error(exception); }
    }

    private void refresh() {
        String query = search.getValue().trim().toLowerCase();
        List<ClienteDTO> data = service.listar();
        if (!query.isEmpty()) data = data.stream().filter(client -> (client.nombre() + " " + client.apellido() + " " + client.dni() + " " + client.email()).toLowerCase().contains(query)).toList();
        grid.setItems(data);
    }
}
