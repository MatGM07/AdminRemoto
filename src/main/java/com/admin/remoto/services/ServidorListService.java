package com.admin.remoto.services;

import com.admin.remoto.SessionManager;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;

@Service
public class ServidorListService {
    private final ServidorService servidorService;
    private final SessionManager sessionManager;

    @Autowired
    public ServidorListService(ServidorService servidorService, SessionManager sessionManager) {
        this.servidorService = servidorService;
        this.sessionManager = sessionManager;
    }

    public List<Servidor> obtenerServidoresUsuario() {
        Usuario current = sessionManager.getUsuario();
        if (current == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        return servidorService.obtenerPorUsuario(current);
    }

    public Servidor agregarServidor(String host, String puerto) {
        Usuario current = sessionManager.getUsuario();
        if (current == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }

        Servidor nuevo = new Servidor();
        nuevo.setDireccion(host);
        nuevo.setPuerto(puerto);
        nuevo.setUsuario(current);

        return servidorService.guardar(nuevo);
    }

    public void eliminarServidor(Long servidorId) {
        servidorService.eliminarPorId(servidorId);
    }

    public void conectarServidor(String host, int puerto) {
        // Lógica para establecer conexión con servidor remoto
        // (Podría moverse a un servicio separado)
    }
}