package com.example.demo.ui;

import com.example.demo.dto.ConfiguracionComercioDTO;
import com.example.demo.dto.PresetRubro;
import com.example.demo.dto.UsuarioDTO;
import com.example.demo.services.ConfiguracionComercioService;
import com.example.demo.services.PresetRubroService;
import com.example.demo.services.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("setup")
@PageTitle("Asistente de configuración | Franco")
public class SetupWizardView extends VerticalLayout implements BeforeEnterObserver {
    private final ConfiguracionComercioService configuracion;
    private final PresetRubroService presets;
    private final UsuarioService usuarios;
    private int step;
    private final VerticalLayout body = new VerticalLayout();
    private PresetRubro chosenPreset;
    private ConfiguracionComercioDTO draft;

    public SetupWizardView(ConfiguracionComercioService configuracion, PresetRubroService presets,
                           UsuarioService usuarios) {
        this.configuracion = configuracion;
        this.presets = presets;
        this.usuarios = usuarios;
        addClassName("content-view");
        addClassName("setup-wizard");
        setWidth("min(720px, 100%)");
        getStyle().set("margin", "0 auto");
        draft = configuracion.obtener();
        add(new H2("Asistente de configuración"),
                new Paragraph("Tres pasos para dejar el comercio listo: plantilla, datos y usuario dueño."),
                body);
        showStep();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!UserSession.isLoggedIn()) {
            event.rerouteTo(LoginView.class);
            return;
        }
        if (!UserSession.isSuperAdmin()) {
            event.rerouteTo(DashboardView.class);
        }
    }

    private void showStep() {
        body.removeAll();
        switch (step) {
            case 0 -> stepPreset();
            case 1 -> stepComercio();
            default -> stepUsuario();
        }
    }

    private void stepPreset() {
        ComboBox<PresetRubro> preset = new ComboBox<>("Plantilla por rubro");
        preset.setItems(PresetRubro.disponibles());
        preset.setItemLabelGenerator(PresetRubro::nombre);
        preset.setWidthFull();
        preset.setValue(chosenPreset);
        Paragraph detail = new Paragraph(chosenPreset == null ? "" : chosenPreset.descripcion());
        preset.addValueChangeListener(e -> {
            chosenPreset = e.getValue();
            detail.setText(chosenPreset == null ? "" : chosenPreset.descripcion());
        });
        Button next = new Button("Siguiente", e -> {
            if (preset.getValue() == null) {
                ViewSupport.error(new IllegalArgumentException("Elegí una plantilla"));
                return;
            }
            chosenPreset = preset.getValue();
            try {
                presets.aplicar(chosenPreset, UserSession.getUser().id());
                draft = configuracion.obtener();
                step = 1;
                showStep();
            } catch (RuntimeException ex) {
                ViewSupport.error(ex);
            }
        });
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        body.add(new Paragraph("Paso 1 de 3 · Plantilla"), preset, detail, next);
    }

    private void stepComercio() {
        TextField name = field("Nombre comercial", draft.nombre());
        TextField cuit = field("CUIT", draft.cuit());
        ComboBox<String> iva = new ComboBox<>("Condición IVA");
        iva.setItems("Responsable Inscripto", "Monotributista", "Exento", "Consumidor Final", "No aplica");
        iva.setValue(draft.condicionIva() == null ? "Monotributista" : draft.condicionIva());
        TextField address = field("Dirección", draft.direccion());
        TextField phone = field("Teléfono", draft.telefono());
        TextField logo = field("URL del logo (opcional)", draft.logoUrl());
        Button back = new Button("Atrás", e -> { step = 0; showStep(); });
        Button next = new Button("Siguiente", e -> {
            try {
                draft = configuracion.guardar(new ConfiguracionComercioDTO(
                        name.getValue(), chosenPreset == null ? draft.rubro() : chosenPreset.rubro(), draft.slogan(),
                        draft.razonSocial(), cuit.getValue(), iva.getValue(), address.getValue(), phone.getValue(),
                        draft.email(), draft.whatsapp(), draft.sitioWeb(), logo.getValue(),
                        draft.colorPrimario(), draft.colorSecundario(), draft.encabezadoTicket(), draft.mensajeTicket(),
                        draft.anchoTicket(), draft.moneda(), draft.zonaHoraria(), draft.mostrarDatosFiscalesTicket(),
                        false, draft.mostrarIvaTicket()));
                step = 2;
                showStep();
            } catch (RuntimeException ex) {
                ViewSupport.error(ex);
            }
        });
        next.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        body.add(new Paragraph("Paso 2 de 3 · Datos del comercio"), name, cuit, iva, address, phone, logo,
                new HorizontalLayout(back, next));
    }

    private void stepUsuario() {
        var current = UserSession.getUser();
        TextField nombre = field("Nombre", current.nombre());
        TextField apellido = field("Apellido", current.apellido());
        EmailField email = new EmailField("Email");
        email.setValue(current.email() == null ? "" : current.email());
        email.setWidthFull();
        PasswordField password = new PasswordField("Nueva contraseña (opcional)");
        password.setWidthFull();
        Button back = new Button("Atrás", e -> { step = 1; showStep(); });
        Button finish = new Button("Finalizar", e -> {
            try {
                Integer dni = usuarios.listar().stream()
                        .filter(u -> u.id().equals(current.id()))
                        .map(UsuarioDTO::dni)
                        .findFirst()
                        .orElse(null);
                usuarios.guardar(current.id(),
                        new UsuarioDTO(current.id(), nombre.getValue(), apellido.getValue(), dni,
                                email.getValue(), "SUPER_ADMIN", true),
                        password.getValue());
                configuracion.marcarSetupCompletado();
                ViewSupport.success("Configuración inicial lista");
                getUI().ifPresent(ui -> ui.navigate(DashboardView.class));
            } catch (RuntimeException ex) {
                ViewSupport.error(ex);
            }
        });
        finish.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        body.add(new Paragraph("Paso 3 de 3 · Usuario dueño"), nombre, apellido, email, password,
                new HorizontalLayout(back, finish));
    }

    private TextField field(String label, String value) {
        TextField field = new TextField(label);
        field.setWidthFull();
        if (value != null) field.setValue(value);
        return field;
    }
}
