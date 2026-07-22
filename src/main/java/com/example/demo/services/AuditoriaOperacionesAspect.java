package com.example.demo.services;

import com.example.demo.dto.AuthResponseDTO;
import com.example.demo.repository.PersonaRepository;
import com.example.demo.ui.UserSession;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Locale;

@Aspect
@Component
public class AuditoriaOperacionesAspect {
    private final AuditoriaService auditoria;
    private final PersonaRepository personas;

    public AuditoriaOperacionesAspect(AuditoriaService auditoria, PersonaRepository personas) {
        this.auditoria = auditoria;
        this.personas = personas;
    }

    @Around("execution(public * com.example.demo.services..*(..)) && !within(com.example.demo.services.AuditoriaService) && !within(com.example.demo.services.AuditoriaOperacionesAspect)")
    public Object registrarOperacion(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (transactional == null || transactional.readOnly() || yaRegistraDetalle(joinPoint, method))
            return joinPoint.proceed();

        Object result = joinPoint.proceed();
        Long usuarioId = usuarioActual(result);
        String entidad = joinPoint.getTarget().getClass().getSimpleName().replace("Service", "").toUpperCase(Locale.ROOT);
        auditoria.registrar(usuarioId, accion(method.getName()), entidad, id(result), "Operación: " + method.getName());
        return result;
    }

    private boolean yaRegistraDetalle(ProceedingJoinPoint joinPoint, Method method) {
        String key = joinPoint.getTarget().getClass().getSimpleName() + "." + method.getName();
        return key.equals("EmpleadoService.guardar") || key.equals("ReglasOperativasService.guardar")
                || key.equals("DevolucionService.crear") || key.equals("CuentaCorrienteService.registrarPago")
                || key.equals("CajaService.abrir") || key.equals("CajaService.cerrar")
                || key.equals("PresetRubroService.aplicar");
    }

    private Long usuarioActual(Object result) {
        if (result instanceof AuthResponseDTO auth) return auth.id();
        try {
            AuthResponseDTO auth = UserSession.getUser();
            if (auth != null) return auth.id();
        } catch (RuntimeException ignored) {
            // No hay una sesión Vaadin activa, por ejemplo en una llamada REST.
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        return personas.findByEmail(authentication.getName()).map(persona -> persona.getId()).orElse(null);
    }

    private Object id(Object result) {
        if (result == null) return null;
        for (String accessor : new String[]{"id", "getId"}) {
            try {
                return result.getClass().getMethod(accessor).invoke(result);
            } catch (ReflectiveOperationException ignored) {
                // El resultado no expone un identificador.
            }
        }
        return null;
    }

    private String accion(String method) {
        String value = method.toLowerCase(Locale.ROOT);
        if (value.startsWith("crear") || value.startsWith("registrar") || value.startsWith("abrir")) return "CREAR";
        if (value.startsWith("eliminar") || value.startsWith("anular")) return "ELIMINAR";
        if (value.startsWith("cerrar")) return "CERRAR";
        return "MODIFICAR";
    }
}
