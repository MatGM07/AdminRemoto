package com.admin.remoto.swing;

import com.admin.remoto.Observador.Observador;
import com.admin.remoto.controller.LoginController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
@Component
public class LoginPanel extends JPanel implements Observador<String,Object> {

    private JTextField nombreField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    private Runnable onLoginSuccess;

    private final LoginController loginController;

    @Autowired
    public LoginPanel(LoginController loginController) {
        this.loginController = loginController;
        this.loginController.agregarObservador(this);

        iniciarUI();
    }

    private void iniciarUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        add(new JLabel("Nombre:"), gbc);
        nombreField = new JTextField(20);
        gbc.gridx = 1;
        add(nombreField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        add(new JLabel("Contraseña:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        add(passwordField, gbc);

        loginButton = new JButton("Iniciar sesión");
        gbc.gridx = 1; gbc.gridy = 2;
        add(loginButton, gbc);

        messageLabel = new JLabel(" ");
        gbc.gridx = 1; gbc.gridy = 3;
        add(messageLabel, gbc);

        loginButton.addActionListener(e -> realizarLogin());
    }

    private void realizarLogin() {
        String nombre = nombreField.getText();
        String contrasena = new String(passwordField.getPassword());

        if (nombre.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor, introduce nombre y contraseña");
            return;
        }
        setLoadingState(true);
        loginController.autenticarUsuario(nombre, contrasena);
    }

    @Override
    public void actualizar(String event, Object data) {
        SwingUtilities.invokeLater(() -> {
            setLoadingState(false);
            switch (event) {
                case "LOGIN_SUCCESS":
                    resetFields();
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                    break;
                case "LOGIN_ERROR":
                    resetFields();
                    mostrarError(data.toString());
                    break;
            }
        });
    }

    public void mostrarError(String mensaje) {
        mostrarMensaje(mensaje, Color.RED);
    }

    private void mostrarMensaje(String mensaje, Color color) {
        messageLabel.setText(mensaje);
        messageLabel.setForeground(color);
    }

    public void setLoadingState(boolean loading) {
        loginButton.setEnabled(!loading);
        messageLabel.setText(loading ? "Conectando..." : " ");
    }

    public void resetFields() {
        nombreField.setText("");
        passwordField.setText("");
        messageLabel.setText(" ");
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }
}