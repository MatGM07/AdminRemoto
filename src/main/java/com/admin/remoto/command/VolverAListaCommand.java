package com.admin.remoto.command;

import javax.swing.*;
import java.awt.*;

public class VolverAListaCommand implements Command {
    private final JPanel mainPanel;
    private final CardLayout layout;
    private final JFrame frame;

    public VolverAListaCommand(JPanel mainPanel, CardLayout layout, JFrame frame) {
        this.mainPanel = mainPanel;
        this.layout = layout;
        this.frame = frame;
    }

    @Override
    public void execute() {
        layout.show(mainPanel, "servers");
        frame.setTitle("Control Remoto - Servidores");
    }
}
