package com.remoto.reportes.controller;

import com.remoto.reportes.models.Usuario;
import org.springframework.stereotype.Service;

@Service
public class SessionManager {
    private static SessionManager instance;
    private Usuario usuario; // Almacenamos el objeto Usuario

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void clearSession() {
        this.usuario = null;
    }
}
