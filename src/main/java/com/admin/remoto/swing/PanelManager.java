package com.admin.remoto.swing;

import com.admin.remoto.command.*;
import org.springframework.beans.factory.annotation.Autowired;
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

        Command loginExitosoCommand = new LoginExitosoCommand(serverListPanel, mainPanel, cardLayout, frame);
        Command logoutCommand = new LogoutCommand(loginPanel, mainPanel, cardLayout, frame);
        Command mostrarAdminCommand = new MostrarAdministracionCommand(mainPanel, cardLayout, frame);
        Command volverAListaCommand = new VolverAListaCommand(mainPanel, cardLayout, frame);

        loginPanel.setOnLoginSuccess(loginExitosoCommand::execute);
        serverListPanel.setOnLogoutRequested(logoutCommand::execute);
        serverListPanel.setOnConnectRequested(mostrarAdminCommand::execute);
        administracionPanel.setOnVolverALista(volverAListaCommand::execute);

        mainPanel.add(loginPanel, "login");
        mainPanel.add(serverListPanel, "servers");
        mainPanel.add(administracionPanel, "admin");

        cardLayout.show(mainPanel, "login");
        return mainPanel;
    }
}
