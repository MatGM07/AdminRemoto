package com.admin.remoto;

import com.admin.remoto.models.Evento;

import java.awt.AWTEvent;
import java.awt.event.*;

public class EventoConverter {

    public static Evento convert(AWTEvent event) {
        if (event instanceof KeyEvent keyEvent) {
            Evento.Type type = switch (keyEvent.getID()) {
                case KeyEvent.KEY_PRESSED -> Evento.Type.KEY_PRESS;
                case KeyEvent.KEY_RELEASED -> Evento.Type.KEY_RELEASE;
                default -> null;
            };
            if (type != null) {
                return new Evento(type, keyEvent.getKeyCode(), 0, 0, 0, 0);
            }
        } else if (event instanceof MouseEvent mouseEvent) {
            Evento.Type type = switch (mouseEvent.getID()) {
                case MouseEvent.MOUSE_PRESSED -> Evento.Type.MOUSE_PRESS;
                case MouseEvent.MOUSE_RELEASED -> Evento.Type.MOUSE_RELEASE;
                case MouseEvent.MOUSE_MOVED -> Evento.Type.MOUSE_MOVE;
                case MouseEvent.MOUSE_DRAGGED -> Evento.Type.MOUSE_DRAG;
                default -> null;
            };
            if (type != null) {
                return new Evento(type, 0, mouseEvent.getButton(),
                        mouseEvent.getX(), mouseEvent.getY(), 0);
            }
        } else if (event instanceof MouseWheelEvent wheelEvent) {
            return new Evento(Evento.Type.MOUSE_WHEEL, 0, 0,
                    wheelEvent.getX(), wheelEvent.getY(),
                    wheelEvent.getWheelRotation());
        }

        return null; // Evento no reconocido
    }
}
