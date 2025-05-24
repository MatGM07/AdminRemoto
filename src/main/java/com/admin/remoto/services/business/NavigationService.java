package com.admin.remoto.services.business;

import com.admin.remoto.models.Servidor;
import com.admin.remoto.swing.AdministracionPanel;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.function.Supplier;

@Component
public class NavigationService {
    public void abrirVentanaConexion(JPanel parent, Servidor servidor, Supplier<AdministracionPanel> proveedorAdminPanel) {
        AdministracionPanel adminPanel = proveedorAdminPanel.get();
        adminPanel.setOnVolverALista(() -> {
            JFrame adminFrame = (JFrame) SwingUtilities.getWindowAncestor(adminPanel);
            if (adminFrame != null) adminFrame.dispose();
            JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(parent);
            if (mainFrame != null) mainFrame.setVisible(true);
        });
        adminPanel.iniciarConexion(servidor.getDireccion(), Integer.parseInt(servidor.getPuerto()));
        JFrame adminFrame = new JFrame("Control Remoto - " + servidor.getDireccion() + ":" + servidor.getPuerto());
        adminFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        adminFrame.getContentPane().add(adminPanel);
        adminFrame.pack();
        adminFrame.setLocationRelativeTo(null);
        adminFrame.setVisible(true);

        JFrame mainFrame = (JFrame) SwingUtilities.getWindowAncestor(parent);
        if (mainFrame != null) mainFrame.setVisible(false);
    }
}

