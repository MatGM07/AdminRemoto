package com.admin.remoto.swing;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RegisterPanel extends JPanel {

    private JTextField nombreField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JButton backButton;
    private JLabel messageLabel;

    // Callbacks
    private Runnable onRegisterSuccess;
    private Runnable onBackToLogin;

    // URL de tu endpoint de registro
    private static final String REGISTER_URL = "http://localhost:8080/registro";

    public RegisterPanel() {
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

    public void setOnRegisterSuccess(Runnable callback) {
        this.onRegisterSuccess = callback;
    }

    public void setOnBackToLogin(Runnable callback) {
        this.onBackToLogin = callback;
    }

    private void realizarRegistro() {
        String nombre = nombreField.getText().trim();
        String pass   = new String(passwordField.getPassword()).trim();

        if (nombre.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("Debe completar todos los campos");
            return;
        }

        registerButton.setEnabled(false);
        messageLabel.setText("Registrando...");

        new SwingWorker<Boolean,Void>() {
            private String mensaje;

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String body = "nombre=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8)
                            + "&contraseña=" + URLEncoder.encode(pass, StandardCharsets.UTF_8);

                    URL url = new URL(REGISTER_URL);
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
                        // Si no usas redirect, podrías leer el body.
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
                    if (ok && onRegisterSuccess != null) {
                        onRegisterSuccess.run();
                    }
                } catch (Exception ex) {
                    messageLabel.setText("Error interno: " + ex.getMessage());
                } finally {
                    registerButton.setEnabled(true);
                }
            }
        }.execute();
    }
}