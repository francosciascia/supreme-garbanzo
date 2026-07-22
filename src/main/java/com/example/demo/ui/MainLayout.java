package com.example.demo.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import com.example.demo.models.PermisoUsuario.Permiso;
import com.example.demo.services.EmpleadoService;
import com.example.demo.services.ConfiguracionComercioService;
import com.example.demo.services.ReglasOperativasService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private static final Set<String> PERSONAS_ROUTES = Set.of("clientes", "proveedores", "usuarios");
    private static final Set<String> STOCK_ROUTES = Set.of("compras", "inventario", "lotes");

    private final H2 pageTitle = new H2();
    private final Set<Permiso> permisos;
    private final NavGroup personasNav;
    private final NavGroup stockNav;
    private final boolean devolucionesHabilitadas;
    private final boolean controlarVencimientos;

    public MainLayout(EmpleadoService empleadoService, ConfiguracionComercioService configuracionService,
                      ReglasOperativasService reglasService) {
        var usuarioActual = UserSession.getUser();
        permisos = usuarioActual == null ? Set.of() : empleadoService.permisos(usuarioActual.id());
        var configuracion = configuracionService.obtener();
        var reglas = reglasService.obtener();
        devolucionesHabilitadas = reglas.devolucionesHabilitadas();
        controlarVencimientos = reglas.controlarVencimientos();
        UI.getCurrent().getPage().executeJs("document.documentElement.style.setProperty('--lumo-primary-color', $0);"
                        + "document.documentElement.style.setProperty('--lumo-primary-text-color', $0);"
                        + "document.documentElement.style.setProperty('--brand-secondary-color', $1);"
                        + "if(localStorage.getItem('color-mode')==='dark')document.documentElement.setAttribute('theme','dark');",
                configuracion.colorPrimario(), configuracion.colorSecundario());
        setPrimarySection(Section.DRAWER);
        pageTitle.getStyle().set("flex-grow", "1");
        Button theme = new Button(VaadinIcon.ADJUST.create(), event ->
                UI.getCurrent().getPage().executeJs("const root=document.documentElement;"
                        + "const dark=root.getAttribute('theme')==='dark';"
                        + "if(dark)root.removeAttribute('theme');else root.setAttribute('theme','dark');"
                        + "localStorage.setItem('color-mode',dark?'light':'dark');"));
        theme.getElement().setAttribute("aria-label", "Cambiar entre modo día y noche");
        theme.setTooltipText("Día / noche");
        theme.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        addToNavbar(new DrawerToggle(), pageTitle, theme);
        H1 brand = new H1(configuracion.nombre());
        brand.addClassName("brand");
        personasNav = personasGroup();
        stockNav = stockGroup();
        VerticalLayout navigation = new VerticalLayout(
                link("Productos", VaadinIcon.PACKAGE, ProductosView.class),
                personasNav,
                link("Punto de venta", VaadinIcon.CART, VentasView.class));
        if (UserSession.isAdmin() || can(Permiso.GESTIONAR_CAJA))
            navigation.add(link("Caja", VaadinIcon.CASH, CajaView.class));
        if (stockNav != null) navigation.add(stockNav);
        if (devolucionesHabilitadas && (UserSession.isAdmin() || can(Permiso.REALIZAR_DEVOLUCIONES)))
            navigation.add(link("Devoluciones", VaadinIcon.ROTATE_LEFT, DevolucionesView.class));
        if (UserSession.isAdmin() || can(Permiso.VER_REPORTES))
            navigation.add(link("Reportes", VaadinIcon.CHART, ReportesView.class));
        if (UserSession.isSuperAdmin()) {
            navigation.add(link("Auditoría", VaadinIcon.SEARCH, AuditoriaView.class));
            navigation.add(link("Configuración", VaadinIcon.COG, ConfiguracionView.class));
        }
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

    private NavGroup personasGroup() {
        List<Component> items = new ArrayList<>();
        items.add(link("Clientes", VaadinIcon.USERS, ClientesView.class));
        if (UserSession.isAdmin() || can(Permiso.REGISTRAR_COMPRAS))
            items.add(link("Proveedores", VaadinIcon.TRUCK, ProveedoresView.class));
        if (UserSession.isSuperAdmin())
            items.add(link("Usuarios", VaadinIcon.USER, UsuariosView.class));
        return navGroup("Personas", VaadinIcon.GROUP, items);
    }

    private NavGroup stockGroup() {
        List<Component> items = new ArrayList<>();
        if (UserSession.isAdmin() || can(Permiso.REGISTRAR_COMPRAS))
            items.add(link("Compras", VaadinIcon.STOCK, ComprasView.class));
        if (UserSession.isAdmin() || can(Permiso.AJUSTAR_STOCK)) {
            items.add(link("Inventario", VaadinIcon.CLIPBOARD_TEXT, InventarioView.class));
            if (controlarVencimientos)
                items.add(link("Lotes", VaadinIcon.ARCHIVES, LotesView.class));
        }
        return items.isEmpty() ? null : navGroup("Stock", VaadinIcon.STORAGE, items);
    }

    private NavGroup navGroup(String label, VaadinIcon icon, List<Component> items) {
        return new NavGroup(label, icon, items);
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
        if (PERSONAS_ROUTES.contains(path)) personasNav.setOpened(true);
        if (stockNav != null && STOCK_ROUTES.contains(path)) stockNav.setOpened(true);
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
        if ("caja".equals(path)) return UserSession.isAdmin() || can(Permiso.GESTIONAR_CAJA);
        if ("inventario".equals(path)) return UserSession.isAdmin() || can(Permiso.AJUSTAR_STOCK);
        if ("lotes".equals(path)) return controlarVencimientos && (UserSession.isAdmin() || can(Permiso.AJUSTAR_STOCK));
        if ("devoluciones".equals(path)) return devolucionesHabilitadas && (UserSession.isAdmin() || can(Permiso.REALIZAR_DEVOLUCIONES));
        if ("reportes".equals(path)) return UserSession.isAdmin() || can(Permiso.VER_REPORTES);
        return true;
    }

    private static final class NavGroup extends VerticalLayout {
        private final VerticalLayout submenu;

        private NavGroup(String label, VaadinIcon icon, List<Component> items) {
            setPadding(false);
            setSpacing(false);
            setWidthFull();
            addClassName("nav-group");

            submenu = new VerticalLayout();
            submenu.setPadding(false);
            submenu.setSpacing(false);
            submenu.setWidthFull();
            submenu.addClassName("nav-submenu");
            submenu.setVisible(false);
            items.forEach(submenu::add);

            Span summary = new Span();
            summary.addClassName("nav-link");
            summary.addClassName("nav-group-summary");
            summary.getElement().setAttribute("role", "button");
            summary.add(icon.create(), new Span(label));
            Icon chevron = VaadinIcon.ANGLE_DOWN.create();
            chevron.addClassName("nav-group-chevron");
            chevron.setSize("14px");
            summary.add(chevron);
            summary.getElement().addEventListener("click", e -> setOpened(!isOpened()));

            add(summary, submenu);
        }

        private void setOpened(boolean opened) {
            submenu.setVisible(opened);
            if (opened) addClassName("opened");
            else removeClassName("opened");
        }

        private boolean isOpened() {
            return submenu.isVisible();
        }
    }
}
