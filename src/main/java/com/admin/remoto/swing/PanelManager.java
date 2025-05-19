package com.admin.remoto.swing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class PanelManager {
    private final LoginPanel loginPanel;
    private final ServidorListPanel serverListPanel;
    private final AdministracionPanel administracionPanel;

    @Autowired
    public PanelManager(LoginPanel loginPanel, ServidorListPanel serverListPanel, AdministracionPanel administracionPanel) {
        this.loginPanel = loginPanel;
        this.serverListPanel = serverListPanel;
        this.administracionPanel = administracionPanel;
    }

    public JPanel getConfiguredMainPanel(JFrame frame) {
        CardLayout cardLayout = new CardLayout();
        JPanel mainPanel = new JPanel(cardLayout);

        // Configurar callbacks
        loginPanel.setOnLoginSuccess(() -> {
            serverListPanel.actualizarLista();
            cardLayout.show(mainPanel, "servers");
            frame.setTitle("Control Remoto - Servidores");
        });

        serverListPanel.setOnLogoutRequested(() -> {
            loginPanel.resetFields();
            SecurityContextHolder.clearContext();
            cardLayout.show(mainPanel, "login");
            frame.setTitle("Control Remoto - Login");
        });

        serverListPanel.setOnConnectRequested(() -> {
            cardLayout.show(mainPanel, "admin");
            frame.setTitle("Control Remoto - Administración");
        });

        administracionPanel.setOnVolverALista(() -> {
            cardLayout.show(mainPanel, "servers");
            frame.setTitle("Control Remoto - Servidores");
        });

        // Agregar paneles
        mainPanel.add(loginPanel, "login");
        mainPanel.add(serverListPanel, "servers");
        mainPanel.add(administracionPanel, "admin");

        cardLayout.show(mainPanel, "login");
        return mainPanel;
    }
}
