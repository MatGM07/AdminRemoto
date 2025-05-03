package com.admin.remoto.dto;

import javax.swing.*;

public class LoginResult {
    private String nombre;
    private String contrasena;
    private Runnable onLoginSuccess;
    private JLabel messageLabel;
    private JButton loginButton;

    public LoginResult(String nombre, String contrasena, Runnable onLoginSuccess, JLabel messageLabel, JButton loginButton) {
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.onLoginSuccess = onLoginSuccess;
        this.messageLabel = messageLabel;
        this.loginButton = loginButton;
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
        return onLoginSuccess;
    }

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public JLabel getMessageLabel() {
        return messageLabel;
    }

    public void setMessageLabel(JLabel messageLabel) {
        this.messageLabel = messageLabel;
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public void setLoginButton(JButton loginButton) {
        this.loginButton = loginButton;
    }
}
