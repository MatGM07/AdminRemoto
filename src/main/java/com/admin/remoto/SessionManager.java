package com.admin.remoto;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Sesion;
import com.admin.remoto.models.Usuario;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class SessionManager {
    private static SessionManager instance;
    private Usuario usuario; // Almacenamos el objeto Usuario
    private Sesion sesion;
    private Servidor servidor;

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

    public Sesion getSesion() {
        return sesion;
    }

    public void setSesion(Sesion sesion) {
        this.sesion = sesion;
    }

    public Servidor getServidor() {
        return servidor;
    }

    public void setServidor(Servidor servidor) {
        this.servidor = servidor;
    }

    public void clearUsuario() {
        this.usuario = null;
    }

    public void clearSesion(){
        this.sesion = null;
    }

    public void clearServidor(){
        this.servidor = null;
    }
}