package com.admin.remoto.services;

import com.admin.remoto.dto.RegisterResult;
import com.admin.remoto.models.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class RegisterService {
    private final UsuarioService usuarioService;

    @Autowired
    public RegisterService(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public Usuario registrarUsuario(String nombre, String contraseña) {
        // El UsuarioService ya maneja la validación de existencia
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setContraseña(contraseña); // Se encriptará automáticamente

        return usuarioService.registrarUsuario(nuevoUsuario);
    }
}