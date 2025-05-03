package com.admin.remoto.dto;

import javax.swing.*;

public class RegisterResult {
    private String nombre;
    private String contrasena;
    private Runnable onRegisterSuccess;
    private JLabel messageLabel;
    private JButton registerButton;

    public RegisterResult(String nombre, String contrasena, Runnable onLoginSuccess, JLabel messageLabel, JButton loginButton) {
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.onRegisterSuccess = onLoginSuccess;
        this.messageLabel = messageLabel;
        this.registerButton = registerButton;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Runnable getOnLoginSuccess() {
        return onRegisterSuccess;
    }

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onRegisterSuccess = onLoginSuccess;
    }

    public JLabel getMessageLabel() {
        return messageLabel;
    }

    public void setMessageLabel(JLabel messageLabel) {
        this.messageLabel = messageLabel;
    }

    public JButton getLoginButton() {
        return registerButton;
    }

    public void setLoginButton(JButton loginButton) {
        this.registerButton = loginButton;
    }
}


