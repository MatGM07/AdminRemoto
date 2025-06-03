package com.admin.remoto.command;

import com.admin.remoto.ui.ServidorListPanel;

import javax.swing.*;
import java.awt.*;

public class LoginExitosoCommand implements Command {
    private final ServidorListPanel servidorListPanel;
    private final JPanel mainPanel;
    private final CardLayout layout;
    private final JFrame frame;

    public LoginExitosoCommand(ServidorListPanel servidorListPanel, JPanel mainPanel, CardLayout layout, JFrame frame) {
        this.servidorListPanel = servidorListPanel;
        this.mainPanel = mainPanel;
        this.layout = layout;
        this.frame = frame;
    }

    @Override
    public void execute() {
        servidorListPanel.actualizarLista();
        layout.show(mainPanel, "servers");
        frame.setTitle("Control Remoto - Servidores");
    }
}
