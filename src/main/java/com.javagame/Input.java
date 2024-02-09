package com.javagame;

import java.util.HashSet;
import java.util.HashMap;
import javafx.scene.Scene;
import javafx.geometry.Point2D;

final public class Input {
    public Input(Scene scene) {
        m_keys = new HashMap<String, KeyState>();
        m_keysPressed = new HashSet<String>();
        m_keysReleased = new HashSet<String>();
        m_mousePos = new Point2D(0, 0);
        scene.setOnKeyPressed(event -> {
            String key = event.getCode().toString();
            m_keysPressed.add(key);
        });
        scene.setOnKeyReleased(event -> {
            String key = event.getCode().toString();
            m_keysReleased.add(key);
        });
        scene.setOnMousePressed(event -> {
            String key = "MOUSE_" + event.getButton().toString();
            m_keysPressed.add(key);
        });
        scene.setOnMouseReleased(event -> {
            String key = "MOUSE_" + event.getButton().toString();
            m_keysReleased.add(key);
        });
        scene.setOnMouseMoved(event -> {
            m_mousePos = new Point2D(event.getX(), event.getY());
        });
        scene.setOnMouseDragged(event -> {
            m_mousePos = new Point2D(event.getX(), event.getY());
        });
    }
    public void poll() {
        for (KeyState state : m_keys.values()) {
            state.pressed = false;
            state.released = false;
        }
        // turn events into polling
        for (String key : m_keysPressed) {
            KeyState state = m_keys.get(key);
            if (state == null) {
                state = new KeyState();
                m_keys.put(key, state);
            }
            if (state.held) continue;
            state.pressed = true;
            state.held = true;
            state.released = false;
        }
        for (String key : m_keysReleased) {
            KeyState state = m_keys.get(key);
            if (state == null) {
                state = new KeyState();
                m_keys.put(key, state);
            }
            state.pressed = false;
            state.held = false;
            state.released = true;
        }
        m_keysPressed = new HashSet<String>();
        m_keysReleased = new HashSet<String>();
    }
    public boolean isPressed(String key) {
        KeyState state = m_keys.get(key);
        if (state == null) return false;
        return state.pressed;
    }
    public boolean isHeld(String key) {
        KeyState state = m_keys.get(key);
        if (state == null) return false;
        return state.held;
    }
    public boolean isReleased(String key) {
        KeyState state = m_keys.get(key);
        if (state == null) return false;
        return state.released;
    }
    public Point2D getMousePos() {
        return new Point2D(m_mousePos.getX(), m_mousePos.getY());
    }

    private class KeyState {
        boolean pressed = false;
        boolean held = false;
        boolean released = false;
    }

    private Point2D m_mousePos;
    private HashSet<String> m_keysPressed;
    private HashSet<String> m_keysReleased;
    private HashMap<String, KeyState> m_keys; 
}
