package com.admin.remoto.services.business;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.swing.AdministracionPanel;
import com.admin.remoto.swing.ServidorListPanel;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.function.Supplier;

@Component
public class NavigationService {

    public void abrirVentanaConexion(JPanel parent, Servidor servidor, Supplier<AdministracionPanel> proveedorAdminPanel) {
        AdministracionPanel adminPanel = proveedorAdminPanel.get();

        // Iniciar la conexión *antes* de mostrar la ventana
        adminPanel.iniciarConexion(servidor, exito -> {
            // Ya estamos en el EDT porque iniciarConexion termina invocando callback desde done() de SwingWorker,
            // pero aún así envuelvo en invokeLater para unificar
            SwingUtilities.invokeLater(() -> {
                if (!exito) {
                    // En lugar de diálogo modal, mostramos el error en el statusLabel de ServidorListPanel
                    if (parent instanceof ServidorListPanel) {
                        ((ServidorListPanel) parent).mostrarError("Ya existe una conexión con ese servidor.");
                    }
                    return;
                }

                adminPanel.setOnVolverALista(() -> {
                    JFrame adminFrame = (JFrame) SwingUtilities.getWindowAncestor(adminPanel);
                    if (adminFrame != null) adminFrame.dispose();
                    JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(parent);
                    if (mainFrame != null) mainFrame.setVisible(true);
                });

                JFrame adminFrame = new JFrame("Control Remoto - " + servidor.getDireccion() + ":" + servidor.getPuerto());
                adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                adminFrame.getContentPane().add(adminPanel);
                adminFrame.pack();
                adminFrame.setLocationRelativeTo(null);
                adminFrame.setVisible(true);
            });
        });
    }
}

