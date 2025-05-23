package com.admin.remoto.controller;

import com.admin.remoto.services.business.SessionManager;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.services.business.ServidorListService;
import com.admin.remoto.swing.ServidorListPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

@Component
public class ServidorListController {
    private final ServidorListService service;
    private ServidorListPanel panel;
    @Autowired
    private SessionManager sessionManager;

    @Autowired
    public ServidorListController(ServidorListService service, SessionManager sessionManager) {
        this.service = service;
        this.sessionManager = sessionManager;
    }

    public void setPanel(ServidorListPanel panel) {
        this.panel = panel;
    }

    public void cargarServidores() {
        panel.setLoadingState(true);
        new SwingWorker<List<Servidor>, Void>() {
            private String errorMessage;

            @Override
            protected List<Servidor> doInBackground() {
                try {
                    return service.obtenerServidoresUsuario();
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Servidor> servidores = get();
                    if (errorMessage != null) {
                        panel.mostrarError(errorMessage);
                    } else {
                        panel.mostrarServidores(servidores);
                    }
                } catch (Exception ex) {
                    panel.mostrarError("Error al cargar servidores: " + ex.getMessage());
                } finally {
                    panel.setLoadingState(false);
                }
            }
        }.execute();
    }

    public void agregarServidor(String direccion) {
        panel.setLoadingState(true);
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
                        panel.agregarServidorALista(servidor);
                        panel.mostrarMensaje("Servidor agregado: " + servidor.getDireccion() + ":" + servidor.getPuerto());
                    } else {
                        panel.mostrarError(errorMessage);
                    }
                } catch (Exception ex) {
                    panel.mostrarError("Error inesperado al agregar servidor: " + ex.getMessage());
                } finally {
                    panel.setLoadingState(false);
                }
            }
        }.execute();
    }

    public void eliminarServidor(Servidor servidor) {
        panel.setLoadingState(true);
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
                    panel.eliminarServidorDeLista(servidor);
                    panel.mostrarMensaje("Servidor eliminado");
                } else {
                    panel.mostrarError("Error al eliminar servidor: " + errorMessage);
                }
                panel.setLoadingState(false);
            }
        }.execute();
    }

    public void conectarServidor(Servidor servidor) {
        panel.setLoadingState(true);
        sessionManager.setServidor(servidor);
        SwingUtilities.invokeLater(() -> {
            panel.setLoadingState(false);              // quitamos el loading
            panel.abrirVentanaConexion(servidor);      // abre adminPanel en nueva ventana
        });
    }

}