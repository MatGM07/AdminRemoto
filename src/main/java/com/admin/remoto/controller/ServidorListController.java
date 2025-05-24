package com.admin.remoto.controller;

import com.admin.remoto.Observador.Observable;
import com.admin.remoto.Observador.Observador;
import com.admin.remoto.services.business.CargaServer;
import com.admin.remoto.services.business.SessionManager;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.services.panel.ServidorListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ServidorListController implements Observable<String, Object> {
    private final List<Observador<String, Object>> observadores = new ArrayList<>();
    private final ServidorListService service;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    public ServidorListController(ServidorListService service, SessionManager sessionManager) {
        this.service = service;
        this.sessionManager = sessionManager;
    }

    @Override
    public void agregarObservador(Observador<String, Object> obs) {
        observadores.add(obs);
    }

    @Override
    public void eliminarObservador(Observador<String, Object> obs) {
        observadores.remove(obs);
    }

    @Override
    public void notificarObservadores(String evento, Object dato) {
        for (Observador<String, Object> obs : observadores) {
            obs.actualizar(evento, dato);
        }
    }

    public void cargarServidores() {
        new CargaServer(service, this).execute();
    }

    public void agregarServidor(String direccion) {
        notificarObservadores("LOADING", true);
        new SwingWorker<Servidor, Void>() {
            private String errorMessage;

            @Override
            protected Servidor doInBackground() {
                try {
                    String[] parts = direccion.split(":");
                    String host = parts[0];
                    String puerto = parts.length > 1 ? parts[1] : "8081";
                    return service.agregarServidor(host, puerto);
                } catch (Exception ex) {
                    errorMessage = "Formato inválido o error al agregar: " + ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    Servidor servidor = get();
                    if (servidor != null) {
                        notificarObservadores("SERVIDOR_AGREGADO", servidor);
                    } else {
                        notificarObservadores("AGREGAR_ERROR", errorMessage);
                    }
                } catch (Exception ex) {
                    notificarObservadores("AGREGAR_ERROR", "Error inesperado al agregar servidor: " + ex.getMessage());
                } finally {
                    notificarObservadores("LOADING", false);
                }
            }
        }.execute();
    }

    public void eliminarServidor(Servidor servidor) {
        notificarObservadores("LOADING", true);
        new SwingWorker<Void, Void>() {
            private String errorMessage;

            @Override
            protected Void doInBackground() {
                try {
                    service.eliminarServidor(servidor.getId());
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                if (errorMessage == null) {
                    notificarObservadores("SERVIDOR_ELIMINADO", servidor);
                    notificarObservadores("MENSAJE", "Servidor eliminado");
                } else {
                    notificarObservadores("ELIMINAR_ERROR", "Error al eliminar servidor: " + errorMessage);
                }
                notificarObservadores("LOADING", false);
            }
        }.execute();
    }

    public void conectarServidor(Servidor servidor) {
        notificarObservadores("LOADING", true);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                sessionManager.setServidor(servidor);
                return null;
            }

            @Override
            protected void done() {
                notificarObservadores("LOADING", false);
                notificarObservadores("CONECTAR_SERVIDOR", servidor);
            }
        }.execute();
    }
}