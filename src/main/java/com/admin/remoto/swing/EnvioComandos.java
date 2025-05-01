package com.admin.remoto.swing;

import com.admin.remoto.models.Evento;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;


public class EnvioComandos extends JFrame {
    private final JTextArea logArea = new JTextArea(15, 60);
    private WebSocketClient socket;
    private ObjectMapper mapper = new ObjectMapper();
    private String serverHost;
    private int serverPort;

    public EnvioComandos(String host, int port) throws Exception {
        super("Cliente de Control Remoto - " + host + ":" + port);
        this.serverHost = host;
        this.serverPort = port;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        logArea.setEditable(false);
        getContentPane().add(new JScrollPane(logArea), BorderLayout.CENTER);

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
                    // si quisieras procesar respuestas del servidor...
                    log("SOCKET-RX", msg);
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

    private void log(String prefix, String msg) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(String.format("[%s] %s%n", prefix, msg));
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

}