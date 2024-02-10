package com.javagame;

import javafx.geometry.Point2D;

final public class Player {
    public Player(double x, double y) {
        m_speed = 0.002;
        m_sensitivity = 0.05;
        m_dir = new Point2D(1, 0).normalize();
        m_body = new RigidBody(new Point2D(x, y), 0.05, 0.2, 0.2);
    }
    void update(Input input, double dt) {
        // direction
        Point2D mousePos = input.getMousePos();
        if (m_mouseOld == null) {
            m_mouseOld = mousePos;
        }
        if (input.isHeld("MOUSE_SECONDARY")) {
            Point2D diff = mousePos.subtract(m_mouseOld).multiply(m_sensitivity);
            // dont care about Y axis (for now atleast)
            double angle = diff.getX() * m_sensitivity;
            m_dir = new Point2D(Math.cos(angle) * m_dir.getX() - Math.sin(angle) * m_dir.getY(),
                            Math.sin(angle) * m_dir.getX() + Math.cos(angle) * m_dir.getY());
        }
        m_mouseOld = mousePos;

        // position
        Point2D offset = new Point2D(0, 0);
        if (input.isHeld("W")) offset = offset.add(m_dir);
        if (input.isHeld("S")) offset = offset.subtract(m_dir);
        if (input.isHeld("A")) offset = offset.add(m_dir.getY(), -m_dir.getX());
        if (input.isHeld("D")) offset = offset.subtract(m_dir.getY(), -m_dir.getX());
        double multiplier = 1;
        if (input.isHeld("SHIFT")) multiplier = 3;

        if (offset.magnitude() > 0) {
            m_body.addVelocity(offset.normalize().multiply(m_speed * multiplier * dt));
        }
    }
    public Point2D getPos() {
        return m_body.getPosition();
    }
    public Point2D getDir() {
        return new Point2D(m_dir.getX(), m_dir.getY());
    }
    public void setPos(double x, double y) {
        m_body.setPosition(new Point2D(x, y));
    }
    public void setDir(double x, double y) {
        m_dir = new Point2D(x, y).normalize();
    }
    public RigidBody getRigidBody() {
        return m_body;
    }

    
    private Point2D m_dir;
    private RigidBody m_body;
    private double m_sensitivity;
    private double m_speed;
    private Point2D m_mouseOld;
}
