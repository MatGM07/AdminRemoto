package com.admin.remoto.services.business;

import com.admin.remoto.Observador.Observable;
import com.admin.remoto.models.Servidor;
import com.admin.remoto.services.panel.ServidorListService;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

public class CargaServer implements AsyncAction {
    private final ServidorListService service;
    private final Observable<String, Object> observable;

    public CargaServer(ServidorListService service, Observable<String, Object> observable) {
        this.service = service;
        this.observable = observable;
    }

    @Override
    public void execute() {
        observable.notificarObservadores("LOADING", true);
        new SwingWorker<List<Servidor>, Void>() {
            private String errorMessage;

            @Override
            protected List<Servidor> doInBackground() {
                try {
                    return service.obtenerServidoresUsuario();
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Servidor> servidores = get();
                    if (errorMessage != null) {
                        observable.notificarObservadores("CARGA_ERROR", errorMessage);
                    } else {
                        observable.notificarObservadores("CARGA_EXITOSA", servidores);
                    }
                } catch (Exception e) {
                    observable.notificarObservadores("CARGA_ERROR", "Error al cargar: " + e.getMessage());
                } finally {
                    observable.notificarObservadores("LOADING", false);
                }
            }
        }.execute();
    }
}
