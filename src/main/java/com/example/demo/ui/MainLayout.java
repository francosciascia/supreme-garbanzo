package com.example.demo.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final H2 pageTitle = new H2();

    public MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToNavbar(new DrawerToggle(), pageTitle);
        H1 brand = new H1("Franco");
        brand.addClassName("brand");
        VerticalLayout navigation = new VerticalLayout(
                link("Dashboard", VaadinIcon.DASHBOARD, DashboardView.class),
                link("Productos", VaadinIcon.PACKAGE, ProductosView.class),
                link("Categorías", VaadinIcon.TAGS, CategoriasView.class),
                link("Clientes", VaadinIcon.USERS, ClientesView.class),
                link("Ventas", VaadinIcon.CART, VentasView.class));
        navigation.setPadding(false);
        var user = UserSession.getUser();
        Span identity = new Span(user == null ? "" : user.nombre() + " " + user.apellido() + " · " + user.rol());
        identity.addClassName("user-identity");
        Button logout = new Button("Cerrar sesión", VaadinIcon.SIGN_OUT.create(), event -> {
            UserSession.logout();
            UI.getCurrent().getPage().setLocation("login");
        });
        logout.setWidthFull();
        VerticalLayout footer = new VerticalLayout(identity, logout);
        footer.addClassName("drawer-footer");
        VerticalLayout drawer = new VerticalLayout(brand, navigation, footer);
        drawer.setSizeFull();
        drawer.expand(navigation);
        addToDrawer(drawer);
    }

    private RouterLink link(String label, VaadinIcon icon, Class<? extends Component> view) {
        RouterLink link = new RouterLink();
        link.add(icon.create(), new Span(label));
        link.setRoute(view);
        link.addClassName("nav-link");
        return link;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!UserSession.isLoggedIn()) {
            event.rerouteTo(LoginView.class);
            return;
        }
        String path = event.getLocation().getPath();
        pageTitle.setText(path.isBlank() ? "Dashboard" : Character.toUpperCase(path.charAt(0)) + path.substring(1));
    }
}
