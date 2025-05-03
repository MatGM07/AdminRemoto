package com.admin.remoto.swing;

import com.admin.remoto.models.Evento;
import com.admin.remoto.websocket.ClienteWebSocket;
import com.admin.remoto.swing.service.EventoService;
import com.admin.remoto.swing.service.ImageService;
import com.admin.remoto.swing.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class AdministracionPanel extends JFrame {
    private final JLabel imageLabel = new JLabel();
    private final JTextArea logArea = new JTextArea(15, 60);
    private WebSocketClient socket;
    private final ObjectMapper mapper = new ObjectMapper();
    private String host;
    private int port;
    public AdministracionPanel(String host, int port) {
        super("Cliente de Control Remoto - " + host + ":" + port);
        this.host = host;
        this.port = port;

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JButton backButton = new JButton("Volver a la lista de servidores");
        backButton.addActionListener(e -> volverAServidores());
        getContentPane().add(backButton, BorderLayout.SOUTH);

        JScrollPane imagePane = new JScrollPane(imageLabel);
        imagePane.setPreferredSize(new Dimension(1024, 768));
        getContentPane().add(imagePane, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Intentar conectar usando el servicio WebSocket
        try {
            log("SOCKET", "Intentando conectar a " + host + ":" + port);
            socket = WebSocketService.conectarPorWebSocket(host, port, this); // Usar WebSocketService para conectar
        } catch (Exception e) {
            log("SOCKET-ERR", "Error al conectar: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        registrarListenerEventosAWT();
    }

    private void volverAServidores() {
        if (socket != null && socket.isOpen()) {
            socket.close();
        }
        dispose();
        JFrame frame = new JFrame("Control Remoto - Selección de Servidor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ServidorList());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void registrarListenerEventosAWT() {
        long mask = AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK |
                AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK;

        Toolkit.getDefaultToolkit().addAWTEventListener(this::procesarEventoAWT, mask);
    }

    private void procesarEventoAWT(AWTEvent evt) {
        if (!(evt.getSource() instanceof Component)) return;
        Window w = SwingUtilities.getWindowAncestor((Component) evt.getSource());
        if (w != this) return;

        try {
            Evento evento = EventoService.construirEventoDesdeAWT(evt);
            if (evento != null && socket.isOpen()) {
                String json = mapper.writeValueAsString(evento);
                socket.send(json);
                log("TX", json);
            }
        } catch (Exception ex) {
            log("ERROR", ex.getMessage());
        }
    }

    public void displayImage(ByteBuffer buffer) {
        try {
            BufferedImage img = ImageService.decodeImage(buffer);
            if (img != null) {
                ImageService.displayImage(imageLabel, img, getContentPane().getSize());
            } else {
                log("ERROR-IMG", "No se pudo decodificar la imagen recibida");
            }
        } catch (Exception e) {
            log("ERROR-IMG", "Error al procesar imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void log(String prefix, String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%s] %s%n", prefix, msg));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}
