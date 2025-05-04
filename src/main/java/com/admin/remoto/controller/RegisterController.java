package com.admin.remoto.controller;

import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.RegisterService;
import com.admin.remoto.swing.RegisterPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

@Component
public class RegisterController {
    private final RegisterService registerService;
    private RegisterPanel registerPanel;

    @Autowired
    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }

    public void setRegisterPanel(RegisterPanel registerPanel) {
        this.registerPanel = registerPanel;
    }

    public void registrarUsuario(String nombre, String contraseña) {
        new SwingWorker<Boolean, Void>() {
            private String errorMessage;
            private Usuario usuarioRegistrado;

            @Override
            protected Boolean doInBackground() {
                try {
                    usuarioRegistrado = registerService.registrarUsuario(nombre, contraseña);
                    return true;
                } catch (IllegalArgumentException ex) {
                    errorMessage = ex.getMessage();
                    return false;
                } catch (RuntimeException ex) {
                    errorMessage = "Error en el servidor. Intente más tarde";
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    if (get()) { // Si devolvió true
                        registerPanel.onRegistroExitoso();
                    } else {
                        registerPanel.mostrarError(errorMessage);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    registerPanel.mostrarError("Error al procesar la solicitud");
                } finally {
                    registerPanel.setLoadingState(false);
                }
            }
        }.execute();
    }
}