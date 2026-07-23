package com.example.demo.ui;

import com.example.demo.services.CajaService;
import com.example.demo.services.ConfiguracionComercioService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/** Layout mínimo para el POS a pantalla completa. */
public class PosLayout extends AppLayout {
    public PosLayout(ConfiguracionComercioService configuracion, CajaService cajaService) {
        setPrimarySection(Section.NAVBAR);
        getElement().getClassList().add("pos-layout");

        var config = configuracion.obtener();
        H1 title = new H1(config.nombre() == null || config.nombre().isBlank() ? "POS" : config.nombre());
        title.addClassName("pos-brand");

        Span mode = new Span("Cobro");
        mode.addClassName("pos-mode-pill");

        Span cajaStatus = new Span();
        cajaStatus.addClassName("pos-caja-chip");
        Long userId = UserSession.getUser() == null ? null : UserSession.getUser().id();
        if (userId != null) {
            cajaService.activa(userId).ifPresentOrElse(
                    c -> {
                        cajaStatus.setText("Caja #" + c.id() + " abierta");
                        cajaStatus.addClassName("pos-caja-ok");
                    },
                    () -> {
                        cajaStatus.setText("Sin caja abierta");
                        cajaStatus.addClassName("pos-caja-warn");
                    });
        }

        Button caja = new Button("Caja", VaadinIcon.CASH.create(),
                e -> UI.getCurrent().navigate(CajaView.class));
        caja.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        Button historial = new Button("Historial", VaadinIcon.LIST.create(),
                e -> UI.getCurrent().navigate(VentasView.class));
        historial.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        Button salir = new Button("Salir", VaadinIcon.ARROW_LEFT.create(),
                e -> UI.getCurrent().navigate(DashboardView.class));
        salir.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

        HorizontalLayout left = new HorizontalLayout(title, mode, cajaStatus);
        left.setAlignItems(HorizontalLayout.Alignment.CENTER);
        left.addClassName("pos-navbar-left");
        left.getStyle().set("flex-grow", "1");

        HorizontalLayout right = new HorizontalLayout(caja, historial, salir);
        right.setAlignItems(HorizontalLayout.Alignment.CENTER);
        right.addClassName("pos-navbar-right");

        addToNavbar(left, right);
    }
}
