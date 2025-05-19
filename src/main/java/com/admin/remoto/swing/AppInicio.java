package com.admin.remoto.swing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;

@Component
public class AppInicio {
    private final FrameInicio frameInicio;

    @Autowired
    public AppInicio(FrameInicio frameInicio) {
        this.frameInicio = frameInicio;
    }

    public void start() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JFrame frame = frameInicio.buildFrame();
            frame.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al iniciar la aplicación: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
