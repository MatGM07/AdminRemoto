package com.admin.remoto.services.panel;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.ui.AdministracionPanel;
import com.admin.remoto.ui.ServidorListPanel;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.function.Supplier;

@Component
public class NavigationService {

    public void abrirVentanaConexion(JPanel parent, Servidor servidor, Supplier<AdministracionPanel> proveedorAdminPanel) {
        AdministracionPanel adminPanel = proveedorAdminPanel.get();

        adminPanel.iniciarConexion(servidor, exito -> {

            SwingUtilities.invokeLater(() -> {
                if (!exito) {

                    if (parent instanceof ServidorListPanel) {
                        ((ServidorListPanel) parent).mostrarError("Error al realizar la conexion.");
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

