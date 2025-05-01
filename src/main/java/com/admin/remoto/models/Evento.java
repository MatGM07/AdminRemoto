package com.admin.remoto.models;

import java.io.Serializable;

public class Evento implements Serializable {
    public enum Type { KEY_PRESS, KEY_RELEASE,
        MOUSE_PRESS, MOUSE_RELEASE,
        MOUSE_MOVE, MOUSE_WHEEL, MOUSE_DRAG }

    private Type type;
    private int keyCode;
    private int mouseButton;
    private int x, y;
    private int wheelRotation;

    // Constructor con todos los campos
    public Evento(Type type, int keyCode, int mouseButton, int x, int y, int wheelRotation) {
        this.type = type;
        this.keyCode = keyCode;
        this.mouseButton = mouseButton;
        this.x = x;
        this.y = y;
        this.wheelRotation = wheelRotation;
    }

    // Getters y setters
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public int getKeyCode() { return keyCode; }
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }

    public int getMouseButton() { return mouseButton; }
    public void setMouseButton(int mouseButton) { this.mouseButton = mouseButton; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWheelRotation() { return wheelRotation; }
    public void setWheelRotation(int wheelRotation) { this.wheelRotation = wheelRotation; }

    @Override
    public String toString() {
        return String.format("[%s] key=%d btn=%d x=%d y=%d wheel=%d",
                type, keyCode, mouseButton, x, y, wheelRotation);
    }
}
