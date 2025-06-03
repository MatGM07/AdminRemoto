package com.remoto.reportes.config;

import com.remoto.reportes.controller.SessionManager;
import com.remoto.reportes.models.Usuario;
import com.remoto.reportes.repositories.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepositorio;
    private final SessionManager sesionManager;

    public LoginSuccessHandler(UsuarioRepository usuarioRepositorio, SessionManager sesionManager) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.sesionManager = sesionManager;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String nombreUsuario = authentication.getName();
        Usuario usuario = usuarioRepositorio.findByNombre(nombreUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado tras login"));

        sesionManager.setUsuario(usuario);

        response.sendRedirect("/home");
    }
}