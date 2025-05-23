package com.admin.remoto.command;

import javax.swing.*;
import java.awt.*;

public class MostrarAdministracionCommand implements Command {
    private final JPanel mainPanel;
    private final CardLayout layout;
    private final JFrame frame;

    public MostrarAdministracionCommand(JPanel mainPanel, CardLayout layout, JFrame frame) {
        this.mainPanel = mainPanel;
        this.layout = layout;
        this.frame = frame;
    }

    @Override
    public void execute() {
        layout.show(mainPanel, "admin");
        frame.setTitle("Control Remoto - Administración");
    }
}