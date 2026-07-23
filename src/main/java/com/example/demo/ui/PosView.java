package com.example.demo.ui;

import com.example.demo.models.PermisoUsuario.Permiso;
import com.example.demo.services.*;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "pos", layout = PosLayout.class)
@PageTitle("POS | Franco")
public class PosView extends VerticalLayout implements BeforeEnterObserver {
    public PosView(VentaService service, ProductoService productoService, ClienteService clienteService,
                   CajaService cajaService, ReglasOperativasService reglasService, EmpleadoService empleadoService) {
        addClassName("pos-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        boolean canDiscount = UserSession.isAdmin()
                || empleadoService.tiene(UserSession.getUser().id(), Permiso.APLICAR_DESCUENTOS);
        var reglas = reglasService.obtener();
        boolean canManualPrice = reglas.permitirPrecioManual()
                && (UserSession.isAdmin() || empleadoService.tiene(UserSession.getUser().id(), Permiso.MODIFICAR_PRECIOS));

        add(new NuevaVentaPanel(service, productoService, clienteService, cajaService, reglasService,
                canDiscount, canManualPrice, true,
                sale -> { },
                () -> getUI().ifPresent(ui -> ui.navigate(VentasView.class))));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!UserSession.isLoggedIn()) event.rerouteTo(LoginView.class);
    }
}
