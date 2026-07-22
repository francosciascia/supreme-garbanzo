package com.example.demo.ui;

import com.example.demo.dto.AuditoriaDTO;
import com.example.demo.services.AuditoriaService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Route(value = "auditoria", layout = MainLayout.class)
@PageTitle("Auditoría | Franco")
public class AuditoriaView extends VerticalLayout {
    private final AuditoriaService service;
    private final DatePicker desde = new DatePicker("Desde");
    private final DatePicker hasta = new DatePicker("Hasta");
    private final ComboBox<String> entidad = new ComboBox<>("Entidad");
    private final ComboBox<String> accion = new ComboBox<>("Acción");
    private final Grid<AuditoriaDTO> grid = new Grid<>(AuditoriaDTO.class, false);

    public AuditoriaView(AuditoriaService service) {
        this.service = service;
        addClassName("content-view");
        setSizeFull();
        entidad.setItems("VENTA", "PRODUCTO", "CAJA", "COMPRA", "DEVOLUCION", "EMPLEADO", "REGLASOPERATIVAS", "CLIENTE");
        entidad.setClearButtonVisible(true);
        accion.setItems("CREAR", "MODIFICAR", "ELIMINAR", "CERRAR");
        accion.setClearButtonVisible(true);
        Button filter = new Button("Filtrar", e -> grid.getDataProvider().refreshAll());
        grid.addColumn(AuditoriaDTO::fecha).setHeader("Fecha y hora");
        grid.addColumn(AuditoriaDTO::usuario).setHeader("Usuario");
        grid.addColumn(AuditoriaDTO::accion).setHeader("Acción");
        grid.addColumn(AuditoriaDTO::entidad).setHeader("Entidad");
        grid.addColumn(AuditoriaDTO::entidadId).setHeader("ID");
        grid.addColumn(AuditoriaDTO::detalle).setHeader("Detalle").setFlexGrow(1);
        grid.setPageSize(50);
        grid.setItems(DataProvider.fromCallbacks(
                q -> service.buscar(desde.getValue(), hasta.getValue(), entidad.getValue(), accion.getValue(), null,
                        PageRequest.of(q.getPage(), q.getPageSize(), Sort.by(Sort.Direction.DESC, "fecha"))).stream(),
                q -> Math.toIntExact(service.buscar(desde.getValue(), hasta.getValue(), entidad.getValue(), accion.getValue(), null,
                        PageRequest.of(0, 1)).getTotalElements())));
        add(ViewSupport.header("Auditoría"), new HorizontalLayout(desde, hasta, entidad, accion, filter), grid);
        expand(grid);
    }
}
