package com.admin.remoto.swing;

import com.admin.remoto.models.Evento;
import com.admin.remoto.websocket.ClienteWebSocket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

public class AdministracionPanel extends JFrame {
    private final JLabel imageLabel = new JLabel();

    private final JTextArea logArea = new JTextArea(15, 60);
    private WebSocketClient socket;
    private ObjectMapper mapper = new ObjectMapper();
    private String host;
    private int port;

    private ByteBuffer imageBuffer;

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

        try {
            log("SOCKET", "Intentando conectar a " + host + ":" + port);
            socket = conectarPorWebSocket();
        } catch (Exception e) {
            log("SOCKET-ERR", "Error al conectar: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        registrarListenerEventosAWT();
    }


    private void volverAServidores(){
        {
            // Cerrar la conexión
            if (socket != null && socket.isOpen()) {
                socket.close();
            }

            // Volver a la pantalla de selección de servidor
            dispose();
            try {
                JFrame frame = new JFrame("Control Remoto - Selección de Servidor");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().add(new ServidorList());
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private WebSocketClient conectarPorWebSocket() throws Exception {
        socket = new ClienteWebSocket(host, port, this);
        boolean connected = socket.connectBlocking();
        if (!connected) {
            log("SOCKET-ERR", "No se pudo establecer conexión con el servidor");
            throw new Exception("No se pudo establecer conexión con el servidor");
        }
        return socket;
    }


    private void registrarListenerEventosAWT() {
        long mask = AWTEvent.KEY_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK;

        Toolkit.getDefaultToolkit().addAWTEventListener(this::procesarEventoAWT, mask);
    }

    private void procesarEventoAWT(AWTEvent evt) {
        if (!(evt.getSource() instanceof Component)) return;
        Window w = SwingUtilities.getWindowAncestor((Component) evt.getSource());
        if (w != this) return;

        try {
            Evento evento = construirEventoDesdeAWT(evt);
            if (evento != null && socket.isOpen()) {
                String json = mapper.writeValueAsString(evento);
                socket.send(json);
                log("TX", json);
            }
        } catch (Exception ex) {
            log("ERROR", ex.getMessage());
        }
    }

    private Evento construirEventoDesdeAWT(AWTEvent evt) {
        if (evt instanceof KeyEvent ke) {
            Evento.Type t = switch (ke.getID()) {
                case KeyEvent.KEY_PRESSED -> Evento.Type.KEY_PRESS;
                case KeyEvent.KEY_RELEASED -> Evento.Type.KEY_RELEASE;
                default -> null;
            };
            if (t != null) return new Evento(t, ke.getKeyCode(), 0, 0, 0, 0);
        } else if (evt instanceof MouseWheelEvent mw) {
            return new Evento(Evento.Type.MOUSE_WHEEL, 0, 0,
                    mw.getXOnScreen(), mw.getYOnScreen(), mw.getWheelRotation());
        } else if (evt instanceof MouseEvent me) {
            Evento.Type t = switch (me.getID()) {
                case MouseEvent.MOUSE_PRESSED  -> Evento.Type.MOUSE_PRESS;
                case MouseEvent.MOUSE_RELEASED -> Evento.Type.MOUSE_RELEASE;
                case MouseEvent.MOUSE_MOVED    -> Evento.Type.MOUSE_MOVE;
                case MouseEvent.MOUSE_DRAGGED  -> Evento.Type.MOUSE_DRAG;
                default                        -> null;
            };
            if (t != null) return new Evento(t, 0, me.getButton(),
                    me.getXOnScreen(), me.getYOnScreen(), 0);
        }
        return null;
    }

    /**
     * Muestra la imagen recibida en el JLabel.
     * @param buffer ByteBuffer con los datos binarios de la imagen JPG
     */
    public void displayImage(ByteBuffer buffer) {
        try {
            BufferedImage img = decodeImage(buffer);
            if (img != null) {
                mostrarImagenEscalada(img);
            } else {
                log("ERROR-IMG", "No se pudo decodificar la imagen recibida");
            }
        } catch (Exception e) {
            log("ERROR-IMG", "Error al procesar imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BufferedImage decodeImage(ByteBuffer buffer) throws IOException {
        byte[] imageData = new byte[buffer.remaining()];
        buffer.get(imageData);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
            return ImageIO.read(bis);
        }
    }

    private void mostrarImagenEscalada(BufferedImage img) {
        SwingUtilities.invokeLater(() -> {
            Dimension panelSize = getContentPane().getSize();
            int maxWidth = panelSize.width - 40;
            int maxHeight = panelSize.height - 100;

            Image scaled = img.getScaledInstance(maxWidth, maxHeight, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
            imageLabel.setPreferredSize(new Dimension(maxWidth, maxHeight));
            imageLabel.revalidate();
        });
    }


    public void log(String prefix, String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%s] %s%n", prefix, msg));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}