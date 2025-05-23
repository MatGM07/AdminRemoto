package com.admin.remoto.services.business;

import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.persistence.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    private final UsuarioService usuarioService;
    private final SessionManager sessionManager;

    @Autowired
    public LoginService(UsuarioService usuarioService, SessionManager sessionManager) {
        this.usuarioService = usuarioService;
        this.sessionManager = sessionManager;
    }

    public Optional<Usuario> autenticar(String nombre, String contrasena) {
        try {
            Usuario usuario = usuarioService.verificarCredenciales(nombre, contrasena);
            if (usuario != null) {
                sessionManager.setUsuario(usuario);
                return Optional.of(usuario);
            }
            return Optional.empty();
        } catch (Exception ex) {
            throw new RuntimeException("Error durante la autenticación: " + ex.getMessage());
        }
    }
}
