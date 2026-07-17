package com.example.demo.ui;

import com.example.demo.dto.LoginDTO;
import com.example.demo.services.AuthService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("login")
@PageTitle("Iniciar sesión | Franco")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthService authService;

    public LoginView(AuthService authService) {
        this.authService = authService;
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout card = new VerticalLayout();
        card.addClassName("login-card");
        card.setWidth("min(420px, calc(100vw - 32px))");
        H1 title = new H1("Franco");
        Paragraph subtitle = new Paragraph("Sistema de gestión comercial");
        EmailField email = new EmailField("Correo electrónico");
        PasswordField password = new PasswordField("Contraseña");
        Button submit = new Button("Ingresar");
        email.setWidthFull();
        password.setWidthFull();
        submit.setWidthFull();
        submit.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submit.addClickListener(event -> login(email.getValue(), password.getValue(), submit));
        password.addKeyPressListener(Key.ENTER, event -> login(email.getValue(), password.getValue(), submit));
        card.add(title, subtitle, email, password, submit);
        add(card);
    }

    private void login(String email, String password, Button submit) {
        submit.setEnabled(false);
        try {
            UserSession.login(authService.login(new LoginDTO(email, password)));
            UI.getCurrent().navigate(DashboardView.class);
        } catch (RuntimeException exception) {
            Notification notification = Notification.show(exception.getMessage(), 3500, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } finally {
            submit.setEnabled(true);
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (UserSession.isLoggedIn()) event.forwardTo(DashboardView.class);
    }
}
