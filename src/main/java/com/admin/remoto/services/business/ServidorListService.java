package com.admin.remoto.services.business;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.models.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServidorListService {
    private final ServidorService servidorService;
    private final SessionManager sessionManager;
    private final AdministracionService administracionService;

    @Autowired
    public ServidorListService(ServidorService servidorService,
                               SessionManager sessionManager,
                               AdministracionService administracionService) {
        this.servidorService = servidorService;
        this.sessionManager = sessionManager;
        this.administracionService = administracionService;
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
        Usuario current = sessionManager.getUsuario();
        if (current == null) {
            throw new IllegalStateException("No hay usuario autenticado");
        }
        servidorService.eliminarPorId(servidorId);
    }

}
