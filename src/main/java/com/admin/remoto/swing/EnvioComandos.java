package com.admin.remoto.swing;

import com.admin.remoto.models.Evento;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.ByteBuffer;

public class EnvioComandos extends JFrame {
    private final JLabel imageLabel = new JLabel();

    private final JTextArea logArea = new JTextArea(15, 60);
    private WebSocketClient socket;
    private ObjectMapper mapper = new ObjectMapper();
    private String serverHost;
    private int serverPort;

    // Buffer para recibir datos binarios de la imagen
    private ByteBuffer imageBuffer;

    public EnvioComandos(String host, int port) throws Exception {
        super("Cliente de Control Remoto - " + host + ":" + port);
        this.serverHost = host;
        this.serverPort = port;

        setDefaultCloseOperation(EXIT_ON_CLOSE);


        // Botón para desconectar y volver a la lista de servidores
        JButton backButton = new JButton("Volver a la lista de servidores");
        backButton.addActionListener(e -> {
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
        });
        getContentPane().add(backButton, BorderLayout.SOUTH);

        JScrollPane imagePane = new JScrollPane(imageLabel);
        imagePane.setPreferredSize(new Dimension(1024, 768)); // tamaño inicial
        getContentPane().add(imagePane, BorderLayout.CENTER);


        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // 1) Conectar por WebSocket
        try {
            log("SOCKET", "Intentando conectar a " + host + ":" + port);
            socket = new WebSocketClient(new URI("ws://" + host + ":" + port + "/ws")) {
                @Override
                public void onOpen(ServerHandshake sh) {
                    log("SOCKET", "Conectado al servidor");
                }

                @Override
                public void onMessage(String msg) {
                    // Procesar mensajes de texto del servidor
                    log("SOCKET-RX", msg);
                }

                @Override
                public void onMessage(ByteBuffer bytes) {
                    // Procesar mensajes binarios (imágenes) del servidor
                    try {
                        log("SOCKET-IMG", "Recibida imagen de " + bytes.remaining() + " bytes");
                        displayImage(bytes);
                    } catch (Exception e) {
                        log("ERROR-IMG", "Error al procesar imagen: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log("SOCKET", "Cerrado: " + reason);
                }

                @Override
                public void onError(Exception ex) {
                    log("SOCKET-ERR", ex.getMessage());
                }
            };
            boolean connected = socket.connectBlocking();
            if (!connected) {
                log("SOCKET-ERR", "No se pudo establecer conexión con el servidor");
                return;
            }
        } catch (Exception e) {
            log("SOCKET-ERR", "Error al conectar: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 2) Registrar AWTEventListener global
        long mask = AWTEvent.KEY_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK;
        Toolkit.getDefaultToolkit().addAWTEventListener(evt -> {
            // Filtrar solo eventos dentro de esta ventana
            if (!(evt.getSource() instanceof Component)) return;
            Window w = SwingUtilities.getWindowAncestor((Component) evt.getSource());
            if (w != this) return;

            try {
                Evento re = null;
                if (evt instanceof KeyEvent ke) {
                    Evento.Type t =
                            (ke.getID() == KeyEvent.KEY_PRESSED ? Evento.Type.KEY_PRESS :
                                    ke.getID() == KeyEvent.KEY_RELEASED ? Evento.Type.KEY_RELEASE :
                                            null);
                    if (t != null) re = new Evento(t, ke.getKeyCode(), 0, 0, 0, 0);
                }
                else if (evt instanceof MouseWheelEvent mw) {
                    re = new Evento(Evento.Type.MOUSE_WHEEL,
                            0, 0,
                            mw.getXOnScreen(), mw.getYOnScreen(),
                            mw.getWheelRotation());
                }
                else if (evt instanceof MouseEvent me) {
                    Evento.Type t = switch (me.getID()) {
                        case MouseEvent.MOUSE_PRESSED  -> Evento.Type.MOUSE_PRESS;
                        case MouseEvent.MOUSE_RELEASED -> Evento.Type.MOUSE_RELEASE;
                        case MouseEvent.MOUSE_MOVED    -> Evento.Type.MOUSE_MOVE;
                        case MouseEvent.MOUSE_DRAGGED  -> Evento.Type.MOUSE_DRAG;
                        default                        -> null;
                    };
                    if (t != null) re = new Evento(t, 0, me.getButton(),
                            me.getXOnScreen(), me.getYOnScreen(), 0);
                }

                if (re != null && socket.isOpen()) {
                    String json = mapper.writeValueAsString(re);
                    socket.send(json);
                    log("TX", json);
                }
            } catch (Exception ex) {
                log("ERROR", ex.getMessage());
            }
        }, mask);
    }

    /**
     * Muestra la imagen recibida en el JLabel
     * @param buffer ByteBuffer con los datos binarios de la imagen JPG
     */
    private long lastUpdateTime = 0;

    private void displayImage(ByteBuffer buffer) {
        long now = System.currentTimeMillis();
        if (now - lastUpdateTime < 100) return; // no más de 10 fps
        lastUpdateTime = now;

        try {
            byte[] imageData = new byte[buffer.remaining()];
            buffer.get(imageData);

            ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
            BufferedImage img = ImageIO.read(bis);
            bis.close();

            if (img != null) {
                Dimension panelSize = getContentPane().getSize();
                int maxWidth = panelSize.width - 40;
                int maxHeight = panelSize.height - 100;

                Image scaled = img.getScaledInstance(
                        maxWidth, maxHeight, Image.SCALE_SMOOTH);

                ImageIcon icon = new ImageIcon(scaled);

                // Solo esta parte en EDT (mínimo posible)
                SwingUtilities.invokeLater(() -> {
                    imageLabel.setIcon(icon);
                    imageLabel.setPreferredSize(new Dimension(maxWidth, maxHeight));
                    imageLabel.revalidate();
                });
            } else {
                log("ERROR-IMG", "No se pudo decodificar la imagen recibida");
            }
        } catch (Exception e) {
            log("ERROR-IMG", "Error al procesar imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void log(String prefix, String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%s] %s%n", prefix, msg));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}