package com.example.demo.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

final class ViewSupport {
    private ViewSupport() { }

    static HorizontalLayout header(String title, Component... actions) {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(HorizontalLayout.Alignment.CENTER);
        H2 heading = new H2(title);
        header.add(heading);
        header.expand(heading);
        header.add(actions);
        return header;
    }

    static void success(String message) {
        Notification notification = Notification.show(message, 2500, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    static void error(Throwable throwable) {
        String message = throwable.getMessage() == null ? "Ocurrió un error inesperado" : throwable.getMessage();
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    static void confirm(String message, Runnable action) {
        confirm("Confirmar", "Confirmar", message, action, true);
    }

    static void confirm(String title, String acceptLabel, String message, Runnable action, boolean danger) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(title);
        dialog.add(message);
        Button cancel = new Button("Cancelar", event -> dialog.close());
        Button accept = new Button(acceptLabel, event -> {
            dialog.close();
            action.run();
        });
        if (danger) accept.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        else accept.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(cancel, accept);
        dialog.open();
    }
}
