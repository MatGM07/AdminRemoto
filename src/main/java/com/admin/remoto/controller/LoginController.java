package com.admin.remoto.controller;

import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.business.LoginService;
import com.admin.remoto.swing.LoginPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Optional;

@Component
public class LoginController {
    private final LoginService loginService;
    private LoginPanel loginPanel;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    public void setLoginPanel(LoginPanel loginPanel) {
        this.loginPanel = loginPanel;
    }

    public void autenticarUsuario(String nombre, String contrasena) {
        new SwingWorker<Optional<Usuario>, Void>() {
            @Override
            protected Optional<Usuario> doInBackground() {
                return loginService.autenticar(nombre, contrasena);
            }

            @Override
            protected void done() {
                try {
                    Optional<Usuario> result = get();
                    if (result.isPresent()) {
                        loginPanel.onLoginExitoso();
                    } else {
                        loginPanel.mostrarError("Credenciales incorrectas");
                    }
                } catch (Exception ex) {
                    loginPanel.mostrarError("Error durante el login: " + ex.getMessage());
                } finally {
                    loginPanel.setLoadingState(false);
                }
            }
        }.execute();
    }
}
