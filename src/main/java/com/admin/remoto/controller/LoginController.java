package com.admin.remoto.controller;

import com.admin.remoto.Observador.Observable;
import com.admin.remoto.Observador.Observador;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.panel.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class LoginController implements Observable<String,Object> {
    private final LoginService loginService;
    private final List<Observador> observadores = new ArrayList<>();

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public void agregarObservador(Observador observador) {
        observadores.add(observador);
    }

    @Override
    public void eliminarObservador(Observador observador) {
        observadores.remove(observador);
    }

    @Override
    public void notificarObservadores(String event, Object data) {
        for (Observador o : observadores) {
            o.actualizar(event, data);
        }
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
                        notificarObservadores("LOGIN_SUCCESS", result.get());
                    } else {
                        notificarObservadores("LOGIN_ERROR", "Credenciales incorrectas");
                    }
                } catch (Exception ex) {
                    notificarObservadores("LOGIN_ERROR", "Error durante el login: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
