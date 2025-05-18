package com.admin.remoto.swing;

import com.admin.remoto.controller.LoginController;
import com.admin.remoto.dto.LoginResult;
import com.admin.remoto.models.Usuario;
import com.admin.remoto.services.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;

@Component
public class LoginPanel extends JPanel {

    private JTextField nombreField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;


    private final LoginController loginController;

    // Callbacks
    private Runnable onLoginSuccess;

    @Autowired
    public LoginPanel(LoginController loginController) {
        this.loginController = loginController;
        iniciarUI();
        loginController.setLoginPanel(this);
    }

    private void iniciarUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        // Listener de Login

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

    public void onLoginExitoso() {
        if (onLoginSuccess != null) {
            SwingUtilities.invokeLater(onLoginSuccess::run);
        }
    }

    public void mostrarError(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            messageLabel.setText(mensaje);
            messageLabel.setForeground(Color.RED);
        });
    }

    public void setLoadingState(boolean loading) {
        SwingUtilities.invokeLater(() -> {
            loginButton.setEnabled(!loading);
            messageLabel.setText(loading ? "Conectando..." : "");
        });
    }

    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }


    public void resetFields() {
        nombreField.setText("");
        passwordField.setText("");
        messageLabel.setText(" ");
        loginButton.setEnabled(true);
    }
}
