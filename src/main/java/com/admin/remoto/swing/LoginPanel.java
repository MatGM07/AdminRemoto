package com.admin.remoto.swing;

import com.admin.remoto.dto.LoginResult;
import com.admin.remoto.swing.service.LoginService;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private JTextField nombreField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;           // Botón para ir a registro
    private JLabel messageLabel;
    private LoginResult loginResult;
    private LoginService loginService;


    // Callbacks
    private Runnable onLoginSuccess;
    private Runnable onRegisterRequested;     // Callback para petición de registro

    // URL del servidor de autenticación
    private static final String LOGIN_URL = "http://localhost:8080/login";

    public LoginPanel() {
        loginService= new LoginService(LOGIN_URL);

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campo nombre
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Nombre:"), gbc);
        nombreField = new JTextField(20);
        gbc.gridx = 1;
        add(nombreField, gbc);

        // Campo contraseña
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Contraseña:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Botón Login
        loginButton = new JButton("Iniciar sesión");
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(loginButton, gbc);

        // Etiqueta de mensaje
        messageLabel = new JLabel(" ");
        gbc.gridx = 1;
        gbc.gridy = 3;
        add(messageLabel, gbc);

        // Botón Registrarse
        registerButton = new JButton("Registrarse");
        gbc.gridx = 1;
        gbc.gridy = 4;
        add(registerButton, gbc);

        // Listener de Login
        loginButton.addActionListener(e -> realizarLogin());
        // Listener de Registro
        registerButton.addActionListener(e -> {
            if (onRegisterRequested != null) {
                onRegisterRequested.run();
            }
        });

    }
    
    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    public void setOnRegisterRequested(Runnable callback) {
        this.onRegisterRequested = callback;
    }

    private void realizarLogin() {
        String nombre = nombreField.getText();
        String contrasena = new String(passwordField.getPassword());

        if (this.loginService == null)
            System.out.println("LoginService no está inicializado");

        if (nombre.isEmpty() || contrasena.isEmpty()) {
            messageLabel.setText("Por favor, introduce correo y contraseña");
            return;
        }
        loginButton.setEnabled(false);
        messageLabel.setText("Conectando...");

        this.loginResult = new LoginResult(nombre,contrasena,onLoginSuccess,messageLabel,loginButton);

        SwingWorker<Boolean, Void> worker = loginService.login(loginResult);
        worker.execute();
    }
}
