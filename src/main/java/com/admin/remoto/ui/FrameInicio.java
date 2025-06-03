package com.admin.remoto.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class FrameInicio {
    private final PanelManager panelManager;

    @Autowired
    public FrameInicio(PanelManager panelManager) {
        this.panelManager = panelManager;
    }

    public JFrame buildFrame() {
        JFrame frame = new JFrame("Control Remoto - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = panelManager.getConfiguredMainPanel(frame);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);

        return frame;
    }
}
