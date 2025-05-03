package com.admin.remoto.swing.service;

import com.admin.remoto.dto.RegisterResult;

import javax.swing.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RegisterService {

    private final String registerUrl;

    public RegisterService(String registerUrl) {
        this.registerUrl = registerUrl;
    }

    public SwingWorker<Boolean, Void> registrar(RegisterResult registerResult) {
        return new SwingWorker<>() {
            private String mensaje;

            String nombre = registerResult.getNombre();
            String contraseña = registerResult.getContrasena();
            Runnable onSuccess = registerResult.getOnLoginSuccess();
            JLabel messageLabel = registerResult.getMessageLabel();
            JButton registerButton = registerResult.getLoginButton();

            @Override
            protected Boolean doInBackground() {
                try {
                    String body = "nombre=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8)
                            + "&contraseña=" + URLEncoder.encode(contraseña, StandardCharsets.UTF_8);

                    URL url = new URL(registerUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                    }

                    int status = conn.getResponseCode();
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_SEE_OTHER
                            || status == HttpURLConnection.HTTP_MOVED_PERM) {
                        String loc = conn.getHeaderField("Location");
                        if (loc != null && loc.contains("/login")) {
                            mensaje = "Registro exitoso. Vuelve a iniciar sesión.";
                            return true;
                        } else {
                            mensaje = "Error desconocido al registrar";
                            return false;
                        }
                    } else if (status == HttpURLConnection.HTTP_OK) {
                        mensaje = "Respuesta OK, revisa UI web";
                        return false;
                    } else {
                        mensaje = "Error del servidor: " + status;
                        return false;
                    }
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
                    registerButton.setEnabled(true);
                }
            }
        };
    }
}
