package com.admin.remoto.comandos;

import com.admin.remoto.models.Evento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Consumer;

public class ClientTestFrame extends JFrame {
    private final JTextArea logArea = new JTextArea(15, 60);

    public ClientTestFrame() {
        super("Prueba Local de Teclado y Ratón (AWTEventListener)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        logArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(logArea);
        getContentPane().add(scroll, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Registramos un listener global antes de que la ventana reciba eventos
        long eventMask = AWTEvent.KEY_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK;
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent event) {
                // Filtrar para solo este JFrame
                Object src = event.getSource();
                if (src instanceof Component) {
                    Window win = SwingUtilities.getWindowAncestor((Component) src);
                    if (win == ClientTestFrame.this) {
                        if (event instanceof KeyEvent) {
                            KeyEvent e = (KeyEvent) event;
                            String type = (e.getID() == KeyEvent.KEY_PRESSED ? "KEY_PRESS"
                                    : e.getID() == KeyEvent.KEY_RELEASED ? "KEY_RELEASE"
                                    : "KEY_TYPED");
                            log(type, e.getKeyCode(), 0, 0, 0, 0);
                        }
                        else if (event instanceof MouseWheelEvent) {
                            MouseWheelEvent e = (MouseWheelEvent) event;
                            log("MOUSE_WHEEL", 0, 0, e.getXOnScreen(), e.getYOnScreen(), e.getWheelRotation());
                        }
                        else if (event instanceof MouseEvent) {
                            MouseEvent e = (MouseEvent) event;
                            String type = switch (e.getID()) {
                                case MouseEvent.MOUSE_PRESSED   -> "MOUSE_PRESS";
                                case MouseEvent.MOUSE_RELEASED  -> "MOUSE_RELEASE";
                                case MouseEvent.MOUSE_MOVED     -> "MOUSE_MOVE";
                                case MouseEvent.MOUSE_DRAGGED   -> "MOUSE_DRAG";
                                default                         -> "MOUSE_EVT";
                            };
                            log(type, 0, e.getButton(), e.getXOnScreen(), e.getYOnScreen(), 0);
                        }
                    }
                }
            }
        }, eventMask);
    }

    private void log(String type, int keyCode, int button, int x, int y, int wheel) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(
                    String.format("[%s] key=%d btn=%d x=%d y=%d wheel=%d%n",
                            type, keyCode, button, x, y, wheel)
            );
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientTestFrame::new);
    }
}