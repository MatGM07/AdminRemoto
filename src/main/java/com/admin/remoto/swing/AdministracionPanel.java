package com.admin.remoto.swing;

import com.admin.remoto.controller.AdministracionController;
import com.admin.remoto.services.EscaladoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.ByteBuffer;


@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AdministracionPanel extends JPanel {
    private final JLabel imageLabel = new JLabel();
    private final JTextArea logArea = new JTextArea(15, 60);
    private final JButton backButton = new JButton("Volver a la lista de servidores");
    private final JCheckBox autoScrollCheckBox = new JCheckBox("Auto-scroll logs", true);

    private final AdministracionController controller;
    private Runnable onVolverALista;

    @Autowired
    public AdministracionPanel(AdministracionController controller) {
        this.controller = controller;
        initComponents();
        controller.setAdministracionPanel(this);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        JScrollPane imagePane = new JScrollPane(imageLabel);
        imagePane.setPreferredSize(new Dimension(1024, 768));
        add(imagePane, BorderLayout.CENTER);

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logPane = new JScrollPane(logArea);
        logPane.setPreferredSize(new Dimension(500, 0));
        add(logPane, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(backButton, BorderLayout.WEST);
        bottomPanel.add(autoScrollCheckBox, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> volverAListaServidores());
    }

    public void iniciarConexion(String host, int port) {
        controller.conectarAServidor(host, port);
    }

    public void actualizarImagen(BufferedImage img) {
        SwingUtilities.invokeLater(() -> {
            if (img == null) return;

            int panelWidth = imageLabel.getWidth();
            int panelHeight = imageLabel.getHeight();

            if (panelWidth <= 0) panelWidth = 800;
            if (panelHeight <= 0) panelHeight = 600;

            double imgRatio = (double) img.getWidth() / img.getHeight();
            double panelRatio = (double) panelWidth / panelHeight;

            int scaledWidth, scaledHeight;
            if (imgRatio > panelRatio) {
                scaledWidth = panelWidth;
                scaledHeight = (int) (panelWidth / imgRatio);
            } else {
                scaledHeight = panelHeight;
                scaledWidth = (int) (panelHeight * imgRatio);
            }

            BufferedImage scaled = EscaladoService.scaleImage(img, scaledWidth, scaledHeight);
            imageLabel.setIcon(new ImageIcon(scaled));
            revalidate();
        });
    }


    public void log(String prefix, String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%s] %s%n", prefix, msg));

            if (autoScrollCheckBox.isSelected()) {
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    public void mostrarMensaje(String mensaje) {
        log("INFO", mensaje);
    }

    public void mostrarError(String mensaje) {
        log("ERROR", mensaje);
    }

    public void volverAListaServidores() {
        controller.desconectar();
        if (onVolverALista != null) onVolverALista.run();
    }

    public void setOnVolverALista(Runnable callback) {
        this.onVolverALista = callback;
    }
}
