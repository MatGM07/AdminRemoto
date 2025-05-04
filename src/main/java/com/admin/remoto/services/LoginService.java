package com.admin.remoto.services;

import com.admin.remoto.SessionManager;
import com.admin.remoto.dto.LoginResult;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.swing.ServidorListPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
