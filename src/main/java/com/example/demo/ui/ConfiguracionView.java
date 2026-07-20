package com.example.demo.ui;

import com.example.demo.dto.ConfiguracionComercioDTO;
import com.example.demo.services.ConfiguracionComercioService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "configuracion", layout = MainLayout.class)
@PageTitle("Configuración del comercio")
public class ConfiguracionView extends VerticalLayout {
    public ConfiguracionView(ConfiguracionComercioService service) {
        addClassName("content-view");
        setWidthFull();
        ConfiguracionComercioDTO current = service.obtener();

        TextField name = field("Nombre comercial", "Es el nombre que verán empleados y clientes", current.nombre());
        TextField businessType = field("Rubro", "Ej.: kiosco, repuestos de motos, almacén", current.rubro());
        TextField slogan = field("Slogan", "Frase corta debajo del nombre", current.slogan());
        TextField logoUrl = field("URL del logo", "Imagen HTTPS para menú, login y comprobantes", current.logoUrl());
        TextField primaryColor = field("Color principal", "Formato hexadecimal, por ejemplo #2563EB", current.colorPrimario());
        TextField secondaryColor = field("Color secundario", "Se utiliza en fondos y contraste", current.colorSecundario());

        FormLayout identityForm = form(name, businessType, slogan, logoUrl, primaryColor, secondaryColor);
        Details identity = section("Identidad visual", "Personaliza la marca que verá el cliente en todo el sistema.", identityForm);
        identity.setOpened(true);

        TextField legalName = field("Razón social", null, current.razonSocial());
        TextField cuit = field("CUIT", null, current.cuit());
        ComboBox<String> taxCondition = new ComboBox<>("Condición frente al IVA");
        taxCondition.setItems("Responsable Inscripto", "Monotributista", "Exento", "Consumidor Final", "No aplica");
        taxCondition.setAllowCustomValue(true);
        taxCondition.addCustomValueSetListener(event -> taxCondition.setValue(event.getDetail()));
        taxCondition.setValue(value(current.condicionIva()));
        TextField address = field("Dirección", null, current.direccion());
        FormLayout fiscalForm = form(legalName, cuit, taxCondition, address);
        Details fiscal = section("Datos comerciales y fiscales", "Información legal que puede aparecer en los comprobantes.", fiscalForm);

        TextField phone = field("Teléfono", null, current.telefono());
        TextField whatsapp = field("WhatsApp", "Puede ser distinto del teléfono fijo", current.whatsapp());
        EmailField email = new EmailField("Email comercial"); set(email, current.email());
        TextField website = field("Sitio web", "Debe comenzar con http:// o https://", current.sitioWeb());
        Details contact = section("Contacto", "Datos que se muestran al cliente y facilitan consultas.", form(phone, whatsapp, email, website));

        TextArea ticketHeader = new TextArea("Texto superior del comprobante"); set(ticketHeader, current.encabezadoTicket());
        TextArea ticketFooter = new TextArea("Mensaje final"); set(ticketFooter, current.mensajeTicket());
        ComboBox<Integer> width = new ComboBox<>("Ancho del ticket"); width.setItems(58, 80); width.setValue(current.anchoTicket());
        ComboBox<String> currency = new ComboBox<>("Moneda"); currency.setItems("ARS", "USD", "UYU", "BRL", "PYG", "CLP"); currency.setValue(current.moneda());
        TextField zone = field("Zona horaria", "Ej.: America/Argentina/Buenos_Aires", current.zonaHoraria());
        Checkbox showFiscal = new Checkbox("Mostrar datos fiscales en el ticket", current.mostrarDatosFiscalesTicket());
        Details tickets = section("Comprobantes y formato regional", "Define qué información recibe el cliente al imprimir.",
                new VerticalLayout(form(ticketHeader, ticketFooter, width, currency, zone), showFiscal));

        Div preview = new Div(new H2(current.nombre()), new Paragraph(value(current.slogan())));
        preview.addClassName("brand-preview");
        preview.getStyle().set("border-left", "8px solid " + current.colorPrimario())
                .set("background", current.colorSecundario()).set("color", "white");
        name.addValueChangeListener(event -> ((H2) preview.getComponentAt(0)).setText(event.getValue()));
        slogan.addValueChangeListener(event -> ((Paragraph) preview.getComponentAt(1)).setText(event.getValue()));
        primaryColor.addValueChangeListener(event -> setPreview(preview, "border-left", "8px solid " + event.getValue()));
        secondaryColor.addValueChangeListener(event -> setPreview(preview, "background", event.getValue()));

        Button save = new Button("Guardar personalización", event -> {
            try {
                ConfiguracionComercioDTO saved = service.guardar(new ConfiguracionComercioDTO(name.getValue(), businessType.getValue(),
                        slogan.getValue(), legalName.getValue(), cuit.getValue(), taxCondition.getValue(), address.getValue(),
                        phone.getValue(), email.getValue(), whatsapp.getValue(), website.getValue(), logoUrl.getValue(),
                        primaryColor.getValue(), secondaryColor.getValue(), ticketHeader.getValue(), ticketFooter.getValue(),
                        width.getValue(), currency.getValue(), zone.getValue(), showFiscal.getValue()));
                applyTheme(saved);
                ViewSupport.success("Personalización guardada");
            } catch (RuntimeException exception) {
                ViewSupport.error(exception);
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add(ViewSupport.header("Configuración del comercio"), new Paragraph("Estos datos cambian la identidad del producto para cada cliente sin alterar la lógica central."),
                preview, identity, fiscal, contact, tickets, save);
    }

    private Details section(String title, String description, com.vaadin.flow.component.Component content) {
        VerticalLayout body = new VerticalLayout(new Paragraph(description), content);
        body.setPadding(false);
        Details details = new Details(title, body);
        details.setWidthFull();
        return details;
    }

    private FormLayout form(com.vaadin.flow.component.Component... components) {
        FormLayout layout = new FormLayout(components);
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1), new FormLayout.ResponsiveStep("700px", 2));
        return layout;
    }

    private TextField field(String label, String helper, String value) {
        TextField field = new TextField(label);
        if (helper != null) field.setHelperText(helper);
        set(field, value);
        return field;
    }

    private void applyTheme(ConfiguracionComercioDTO config) {
        UI.getCurrent().getPage().executeJs("document.documentElement.style.setProperty('--lumo-primary-color', $0);"
                + "document.documentElement.style.setProperty('--lumo-primary-text-color', $0);"
                + "document.documentElement.style.setProperty('--brand-secondary-color', $1);",
                config.colorPrimario(), config.colorSecundario());
    }

    private void setPreview(Div preview, String property, String value) {
        if (value != null && value.matches(".*#[0-9a-fA-F]{6}.*")) preview.getStyle().set(property, value);
    }

    private void set(com.vaadin.flow.component.HasValue<?, String> field, String value) { field.setValue(value(value)); }
    private String value(String value) { return value == null ? "" : value; }
}
