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
import com.example.demo.models.PermisoUsuario.Permiso;
import com.example.demo.services.EmpleadoService;

import java.util.Set;

public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final H2 pageTitle = new H2();
    private final Set<Permiso> permisos;

    public MainLayout(EmpleadoService empleadoService) {
        var usuarioActual = UserSession.getUser();
        permisos = usuarioActual == null ? Set.of() : empleadoService.permisos(usuarioActual.id());
        setPrimarySection(Section.DRAWER);
        addToNavbar(new DrawerToggle(), pageTitle);
        H1 brand = new H1("Franco");
        brand.addClassName("brand");
        VerticalLayout navigation = new VerticalLayout(
                link("Dashboard", VaadinIcon.DASHBOARD, DashboardView.class),
                link("Productos", VaadinIcon.PACKAGE, ProductosView.class),
                link("Categorías", VaadinIcon.TAGS, CategoriasView.class),
                link("Clientes", VaadinIcon.USERS, ClientesView.class),
                link("Punto de venta", VaadinIcon.CART, VentasView.class),
                link("Caja", VaadinIcon.CASH, CajaView.class));
        if (UserSession.isAdmin() || can(Permiso.REGISTRAR_COMPRAS)) {
            navigation.add(link("Proveedores", VaadinIcon.TRUCK, ProveedoresView.class),
                    link("Compras", VaadinIcon.STOCK, ComprasView.class));
        }
        if (UserSession.isAdmin() || can(Permiso.AJUSTAR_STOCK)) navigation.add(
                link("Inventario", VaadinIcon.CLIPBOARD_TEXT, InventarioView.class),
                link("Lotes", VaadinIcon.ARCHIVES, LotesView.class));
        if (UserSession.isAdmin() || can(Permiso.REALIZAR_DEVOLUCIONES))
            navigation.add(link("Devoluciones", VaadinIcon.ROTATE_LEFT, DevolucionesView.class));
        if (UserSession.isAdmin() || can(Permiso.VER_REPORTES))
            navigation.add(link("Reportes", VaadinIcon.CHART, ReportesView.class));
        if (UserSession.isSuperAdmin()) navigation.add(
                link("Usuarios", VaadinIcon.USER, UsuariosView.class),
                link("Empleados", VaadinIcon.USER_CARD, EmpleadosView.class),
                link("Reglas del negocio", VaadinIcon.SLIDERS, ReglasOperativasView.class),
                link("Auditoría", VaadinIcon.SEARCH, AuditoriaView.class),
                link("Configuración", VaadinIcon.COG, ConfiguracionView.class));
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
        if (!hasAccess(path)) {
            event.rerouteTo(DashboardView.class);
            return;
        }
        pageTitle.setText(path.isBlank() ? "Dashboard" : Character.toUpperCase(path.charAt(0)) + path.substring(1));
    }

    private static final Set<String> SUPER_ADMIN_ROUTES = Set.of(
            "usuarios", "empleados", "reglas", "auditoria", "configuracion");

    private boolean can(Permiso permiso) {
        return permisos.contains(permiso);
    }

    private boolean hasAccess(String path) {
        if (SUPER_ADMIN_ROUTES.contains(path)) return UserSession.isSuperAdmin();
        if (Set.of("proveedores", "compras").contains(path)) return UserSession.isAdmin() || can(Permiso.REGISTRAR_COMPRAS);
        if (Set.of("inventario", "lotes").contains(path)) return UserSession.isAdmin() || can(Permiso.AJUSTAR_STOCK);
        if ("devoluciones".equals(path)) return UserSession.isAdmin() || can(Permiso.REALIZAR_DEVOLUCIONES);
        if ("reportes".equals(path)) return UserSession.isAdmin() || can(Permiso.VER_REPORTES);
        return true;
    }
}
