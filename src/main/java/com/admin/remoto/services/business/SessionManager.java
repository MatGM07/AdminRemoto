package com.admin.remoto.services.business;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Sesion;
import com.admin.remoto.models.Usuario;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {
    private static SessionManager instance;

    private Usuario usuario;


    private final Map<Sesion, Servidor> sesionesActivas = new ConcurrentHashMap<>();

    private final Set<String> direccionesActivas = ConcurrentHashMap.newKeySet();

    private SessionManager() {
    }

    public static synchronized SessionManager getInstance() {
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

    public void clearUsuario() {
        this.usuario = null;
    }


    public synchronized void addSesion(Sesion sesion, Servidor servidor) {
        String clave = servidor.getDireccion() + ":" + servidor.getPuerto();
        if (direccionesActivas.contains(clave)) {
            throw new IllegalStateException(
                    "Ya existe una sesión activa en " + clave + ". No se permite duplicar."
            );
        }
        sesionesActivas.put(sesion, servidor);
        direccionesActivas.add(clave);
    }

    public synchronized void removeSesion(Sesion sesion) {
        Servidor servidor = sesionesActivas.remove(sesion);
        if (servidor != null) {
            String clave = servidor.getDireccion() + ":" + servidor.getPuerto();
            direccionesActivas.remove(clave);
        }
    }

    public Servidor getServidorDeSesion(Sesion sesion) {
        return sesionesActivas.get(sesion);
    }


    public Set<Sesion> getSesionesActivas() {
        return Collections.unmodifiableSet(sesionesActivas.keySet());
    }


    public synchronized void clearSesiones() {
        sesionesActivas.clear();
        direccionesActivas.clear();
    }

    public boolean direccionOcupada(String host, int port) {
        return direccionesActivas.contains(host + ":" + port);
    }
}