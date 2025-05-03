package com.admin.remoto.swing.service;

import com.admin.remoto.models.Evento;
import java.awt.*;
import java.awt.event.*;

public class EventoService {

    public static Evento construirEventoDesdeAWT(AWTEvent evt) {
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
}
