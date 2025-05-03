package com.admin.remoto.swing.service;

import com.admin.remoto.dto.LoginResult;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginService {
    private final String loginUrl;

    public LoginService(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public SwingWorker<Boolean, Void> login(LoginResult loginResult) {
        return new SwingWorker<>() {
            private String mensaje;

            String nombre = loginResult.getNombre();
            String contrasena = loginResult.getContrasena();
            Runnable onSuccess = loginResult.getOnLoginSuccess();
            JLabel messageLabel = loginResult.getMessageLabel();
            JButton loginButton = loginResult.getLoginButton();

            @Override
            protected Boolean doInBackground() {
                try {
                    String body = "username=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8)
                            + "&password=" + URLEncoder.encode(contrasena, StandardCharsets.UTF_8);

                    URL url = new URL(loginUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }

                    int status = conn.getResponseCode();
                    boolean autenticado = false;

                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_SEE_OTHER
                            || status == HttpURLConnection.HTTP_MOVED_PERM) {
                        String location = conn.getHeaderField("Location");
                        autenticado = location != null && !location.contains("login") && !location.contains("error");
                    } else if (status == HttpURLConnection.HTTP_OK) {
                        String cookies = conn.getHeaderField("Set-Cookie");
                        if (cookies != null && cookies.contains("JSESSIONID")) {
                            autenticado = true;
                        } else {
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                autenticado = !response.toString().toLowerCase().contains("error")
                                        && !response.toString().toLowerCase().contains("invalid");
                            }
                        }
                    }

                    mensaje = autenticado ? "Inicio de sesión exitoso" : "Credenciales incorrectas";
                    return autenticado;

                } catch (Exception ex) {
                    mensaje = "Fallo de conexión: " + ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    messageLabel.setText(mensaje);
                    if (ok && onSuccess != null) {
                        onSuccess.run();
                    }
                } catch (Exception ex) {
                    messageLabel.setText("Error interno: " + ex.getMessage());
                } finally {
                    loginButton.setEnabled(true);
                }
            }
        };
    }
}
