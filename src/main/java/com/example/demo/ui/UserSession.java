package com.example.demo.ui;

import com.example.demo.dto.AuthResponseDTO;
import com.vaadin.flow.server.VaadinSession;

public final class UserSession {
    private UserSession() { }

    public static AuthResponseDTO getUser() {
        return VaadinSession.getCurrent().getAttribute(AuthResponseDTO.class);
    }

    public static void login(AuthResponseDTO user) {
        VaadinSession.getCurrent().setAttribute(AuthResponseDTO.class, user);
    }

    public static void logout() {
        VaadinSession.getCurrent().setAttribute(AuthResponseDTO.class, null);
        VaadinSession.getCurrent().getSession().invalidate();
    }

    public static boolean isLoggedIn() { return getUser() != null; }

    public static boolean isAdmin() {
        AuthResponseDTO user = getUser();
        return user != null && ("ADMIN".equals(user.rol()) || "SUPER_ADMIN".equals(user.rol()));
    }

    public static boolean isSuperAdmin() {
        AuthResponseDTO user = getUser();
        return user != null && "SUPER_ADMIN".equals(user.rol());
    }
}
