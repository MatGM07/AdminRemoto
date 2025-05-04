package com.admin.remoto.swing;

import com.admin.remoto.dto.RegisterResult;
import com.admin.remoto.controller.RegisterController;
import com.admin.remoto.services.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class RegisterPanel extends JPanel {

    private JTextField nombreField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton backButton;
    private JLabel messageLabel;

    private Runnable onRegisterSuccess;
    private Runnable onBackToLogin;

    private final RegisterController registerController;

    @Autowired
    public RegisterPanel(RegisterController registerController) {
        this.registerController = registerController;
        iniciarUI();
        registerController.setRegisterPanel(this);
    }

    private void iniciarUI(){
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nombre:"), gbc);
        nombreField = new JTextField(20);
        gbc.gridx = 1;
        add(nombreField, gbc);

        // Contraseña
        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Contraseña:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Mensaje
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        messageLabel = new JLabel(" ");
        add(messageLabel, gbc);

        // Botón Registrar
        registerButton = new JButton("Registrarse");
        gbc.gridy = 3; gbc.gridwidth = 1; gbc.gridx = 0;
        add(registerButton, gbc);

        // Botón Volver
        backButton = new JButton("Volver al Login");
        gbc.gridx = 1;
        add(backButton, gbc);

        registerButton.addActionListener(e -> realizarRegistro());
        backButton.addActionListener(e -> {
            if (onBackToLogin != null) onBackToLogin.run();
        });
    }

    private void realizarRegistro() {
        String nombre = nombreField.getText().trim();
        String contraseña = new String(passwordField.getPassword()).trim();

        if (nombre.isEmpty() || contraseña.isEmpty()) {
            mostrarError("Debe completar todos los campos");
            return;
        }

        setLoadingState(true);
        registerController.registrarUsuario(nombre, contraseña);
    }

    public void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            messageLabel.setText(mensaje);
            messageLabel.setForeground(Color.RED);
        });
    }

    public void setLoadingState(boolean loading) {
        SwingUtilities.invokeLater(() -> {
            registerButton.setEnabled(!loading);
            backButton.setEnabled(!loading);
            messageLabel.setText(loading ? "Registrando..." : "");
        });
    }


    public void onRegistroExitoso() {
        resetFields();
        if (onRegisterSuccess != null) {
            SwingUtilities.invokeLater(onRegisterSuccess::run);
        }
    }

    public void setOnRegisterSuccess(Runnable callback) {
        this.onRegisterSuccess = callback;
    }

    public void setOnBackToLogin(Runnable callback) {
        this.onBackToLogin = callback;
    }


    public void resetFields() {
        nombreField.setText("");
        passwordField.setText("");
        messageLabel.setText(" ");
        registerButton.setEnabled(true);
    }

}