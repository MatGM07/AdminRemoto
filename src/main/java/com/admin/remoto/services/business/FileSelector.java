package com.admin.remoto.services.business;

import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.io.File;

@Service
public class FileSelector {
    public File seleccionarArchivo(Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona un archivo para transferir");

        int result = fileChooser.showOpenDialog(parentComponent);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}
