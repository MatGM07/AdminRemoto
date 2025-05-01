package com.admin.remoto.swing;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LoginPanel extends JPanel {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;           // Botón para ir a registro
    private JLabel messageLabel;

    // Callbacks
    private Runnable onLoginSuccess;
    private Runnable onRegisterRequested;     // Callback para petición de registro

    // URL del servidor de autenticación
    private static final String LOGIN_URL = "http://localhost:8080/login";

    public LoginPanel() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campo correo
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Correo:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        add(emailField, gbc);

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

        // Solo añadir botón de omitir en modo desarrollo
        boolean modoDesarrollo = Boolean.parseBoolean(System.getProperty("app.dev", "false"));
        if (modoDesarrollo) {
            JButton skipButton = new JButton("Omitir login (solo desarrollo)");
            gbc.gridx = 1;
            gbc.gridy = 5;
            add(skipButton, gbc);
            skipButton.addActionListener(e -> {
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
            });
        }
    }

    /** Establece callback para login exitoso */
    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    /** Establece callback cuando se solicita registro */
    public void setOnRegisterRequested(Runnable callback) {
        this.onRegisterRequested = callback;
    }

    /** Ejecuta petición HTTP de login */
    private void realizarLogin() {
        String correo = emailField.getText();
        String contrasena = new String(passwordField.getPassword());

        if (correo.isEmpty() || contrasena.isEmpty()) {
            messageLabel.setText("Por favor, introduce correo y contraseña");
            return;
        }

        loginButton.setEnabled(false);
        messageLabel.setText("Conectando...");

        new SwingWorker<Boolean, Void>() {
            private String mensaje;

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String body = "username=" + URLEncoder.encode(correo, StandardCharsets.UTF_8)
                            + "&password=" + URLEncoder.encode(contrasena, StandardCharsets.UTF_8);

                    URL url = new URL(LOGIN_URL);
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
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER) {
                        String location = conn.getHeaderField("Location");
                        if (location != null && !location.contains("login") && !location.contains("error")) {
                            autenticado = true;
                            mensaje = "Inicio de sesión exitoso";
                        } else {
                            mensaje = "Credenciales incorrectas";
                        }
                    } else if (status == HttpURLConnection.HTTP_OK) {
                        String cookies = conn.getHeaderField("Set-Cookie");
                        if (cookies != null && cookies.contains("JSESSIONID")) {
                            autenticado = true;
                            mensaje = "Inicio de sesión exitoso";
                        } else {
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(conn.getInputStream()))) {
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                if (!response.toString().toLowerCase().contains("error")
                                        && !response.toString().toLowerCase().contains("invalid")) {
                                    autenticado = true;
                                    mensaje = "Inicio de sesión exitoso";
                                } else {
                                    mensaje = "Credenciales incorrectas";
                                }
                            }
                        }
                    } else if (status == HttpURLConnection.HTTP_UNAUTHORIZED
                            || status == HttpURLConnection.HTTP_FORBIDDEN) {
                        mensaje = "Credenciales incorrectas";
                    } else {
                        mensaje = "Error en el servidor: " + status;
                    }
                    return autenticado;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mensaje = "Error de conexión: " + ex.getMessage();
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean autenticado = get();
                    messageLabel.setText(mensaje);
                    if (autenticado && onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                } catch (Exception ex) {
                    messageLabel.setText("Error inesperado: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    loginButton.setEnabled(true);
                }
            }
        }.execute();
    }
}
