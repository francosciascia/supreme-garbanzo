package com.example.demo.ui;

import com.example.demo.dto.CategoriaCUDTO;
import com.example.demo.dto.CategoriaDTO;
import com.example.demo.services.CategoriaService;
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

import java.util.List;

@Route(value = "categorias", layout = MainLayout.class)
@PageTitle("Categorías | Franco")
public class CategoriasView extends VerticalLayout {
    private final CategoriaService service;
    private final Grid<CategoriaDTO> grid = new Grid<>(CategoriaDTO.class, false);
    private final TextField search = new TextField();

    public CategoriasView(CategoriaService service) {
        this.service = service;
        addClassName("content-view");
        setSizeFull();
        Button add = new Button("Nueva categoría", VaadinIcon.PLUS.create(), event -> openForm(null));
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.setVisible(UserSession.isAdmin());
        search.setPlaceholder("Buscar categoría...");
        search.setPrefixComponent(VaadinIcon.SEARCH.create());
        search.setValueChangeMode(ValueChangeMode.LAZY);
        search.addValueChangeListener(event -> refresh());
        grid.addColumn(CategoriaDTO::nombre).setHeader("Nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(CategoriaDTO::descripcion).setHeader("Descripción").setFlexGrow(1);
        grid.addComponentColumn(this::actions).setHeader("Acciones").setAutoWidth(true);
        grid.setSizeFull();
        add(ViewSupport.header("Gestión de categorías", add), search, grid);
        expand(grid);
        refresh();
    }

    private HorizontalLayout actions(CategoriaDTO category) {
        Button edit = new Button(VaadinIcon.EDIT.create(), event -> openForm(category));
        edit.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        edit.setVisible(UserSession.isAdmin());
        Button delete = new Button(VaadinIcon.TRASH.create(), event -> ViewSupport.confirm(
                "¿Eliminar la categoría " + category.nombre() + "?", () -> delete(category.id())));
        delete.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        delete.setVisible(UserSession.isSuperAdmin());
        return new HorizontalLayout(edit, delete);
    }

    private void openForm(CategoriaDTO category) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(category == null ? "Nueva categoría" : "Editar categoría");
        TextField name = new TextField("Nombre");
        TextArea description = new TextArea("Descripción");
        name.setWidthFull();
        description.setWidthFull();
        if (category != null) {
            name.setValue(category.nombre());
            description.setValue(category.descripcion() == null ? "" : category.descripcion());
        }
        dialog.add(new VerticalLayout(name, description));
        Button cancel = new Button("Cancelar", event -> dialog.close());
        Button save = new Button("Guardar", event -> {
            if (name.isEmpty()) { name.setInvalid(true); name.setErrorMessage("El nombre es obligatorio"); return; }
            try {
                CategoriaCUDTO dto = new CategoriaCUDTO(name.getValue(), description.getValue());
                if (category == null) service.crear(dto); else service.actualizar(category.id(), dto);
                dialog.close(); refresh(); ViewSupport.success("Categoría guardada");
            } catch (RuntimeException exception) { ViewSupport.error(exception); }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, save);
        dialog.open();
    }

    private void delete(Long id) {
        try { service.eliminar(id); refresh(); ViewSupport.success("Categoría eliminada"); }
        catch (RuntimeException exception) { ViewSupport.error(exception); }
    }

    private void refresh() {
        String query = search.getValue().trim().toLowerCase();
        List<CategoriaDTO> data = service.listar();
        if (!query.isEmpty()) data = data.stream().filter(category ->
                category.nombre().toLowerCase().contains(query) ||
                (category.descripcion() != null && category.descripcion().toLowerCase().contains(query))).toList();
        grid.setItems(data);
    }
}
