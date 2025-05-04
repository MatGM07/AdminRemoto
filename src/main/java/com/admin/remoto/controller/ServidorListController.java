package com.admin.remoto.controller;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.services.ServidorListService;
import com.admin.remoto.swing.ServidorListPanel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

@Component
public class ServidorListController {
    private final ServidorListService servidorListService;
    private ServidorListPanel servidorListPanel;

    @Autowired
    public ServidorListController(ServidorListService servidorListService) {
        this.servidorListService = servidorListService;
    }

    public void setServidorListPanel(ServidorListPanel servidorListPanel) {
        this.servidorListPanel = servidorListPanel;
    }

    public void cargarServidores() {
        new SwingWorker<List<Servidor>, Void>() {
            private String errorMessage;

            @Override
            protected List<Servidor> doInBackground() {
                try {
                    return servidorListService.obtenerServidoresUsuario();
                } catch (IllegalStateException ex) {
                    errorMessage = ex.getMessage();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Servidor> servidores = get();
                    servidorListPanel.mostrarServidores(servidores);
                } catch (Exception ex) {
                    servidorListPanel.mostrarError(errorMessage != null ? errorMessage : "Error al cargar servidores");
                }
            }
        }.execute();
    }

    public void agregarServidor(String direccion) {
        new SwingWorker<Servidor, Void>() {
            private String errorMessage;

            @Override
            protected Servidor doInBackground() {
                try {
                    String[] partes = direccion.split(":");
                    String host = partes[0];
                    String puerto = partes.length > 1 ? partes[1] : "8081";
                    return servidorListService.agregarServidor(host, puerto);
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
                        servidorListPanel.agregarServidorALista(servidor);
                        servidorListPanel.mostrarMensaje("Servidor agregado: " + servidor.getDireccion());
                    } else {
                        servidorListPanel.mostrarError(errorMessage);
                    }
                } catch (Exception ex) {
                    servidorListPanel.mostrarError("Error inesperado al agregar servidor");
                }
            }
        }.execute();
    }

    public void eliminarServidor(Servidor servidor) {
        new SwingWorker<Void, Void>() {
            private String errorMessage;

            @Override
            protected Void doInBackground() {
                try {
                    servidorListService.eliminarServidor(servidor.getId());
                    return null;
                } catch (Exception ex) {
                    errorMessage = "Error al eliminar servidor: " + ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    servidorListPanel.eliminarServidorDeLista(servidor);
                    servidorListPanel.mostrarMensaje("Servidor eliminado");
                } catch (Exception ex) {
                    servidorListPanel.mostrarError(errorMessage != null ? errorMessage : "Error al eliminar servidor");
                }
            }
        }.execute();
    }

    public void conectarAServidor(Servidor servidor) {
        new SwingWorker<Void, Void>() {
            private String errorMessage;

            @Override
            protected Void doInBackground() {
                try {
                    servidorListService.conectarServidor(
                            servidor.getDireccion(),
                            Integer.parseInt(servidor.getPuerto())
                    );
                    return null;
                } catch (Exception ex) {
                    errorMessage = "Error al conectar: " + ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    servidorListPanel.mostrarMensaje("Conectado a " + servidor.getDireccion());
                    servidorListPanel.abrirVentanaConexion();
                } catch (Exception ex) {
                    servidorListPanel.mostrarError(errorMessage);
                }
            }
        }.execute();
    }
}
