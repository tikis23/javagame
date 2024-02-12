package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

final public class RigidBody {
    public RigidBody(Point2D position, double maxSpeed, double radius, double velocityDamp) {
        m_position = new Point2D(position.getX(), position.getY());
        m_velocity = new Point2D(0, 0);
        m_maxSpeed = maxSpeed;
        m_collisionRadius = radius;
        m_id = m_idCounter;
        m_idCounter++;
        m_damp = velocityDamp;
        collisionFlags = EnumSet.noneOf(Physics.CollideMask.class);
    }
    public int getId() {
        return m_id;
    }
    public Point2D getPosition() {
        return new Point2D(m_position.getX(), m_position.getY());
    }
    public void setPosition(Point2D position) {
        m_position = new Point2D(position.getX(), position.getY());
    }
    public Point2D getVelocity() {
        return new Point2D(m_velocity.getX(), m_velocity.getY());
    }
    public void setVelocity(Point2D velocity) {
        m_velocity = new Point2D(velocity.getX(), velocity.getY());
        // cap speed
        if (m_velocity.magnitude() > m_maxSpeed) {
            m_velocity = m_velocity.normalize().multiply(m_maxSpeed);
        }
    }
    public void addVelocity(Point2D velocity) {
        setVelocity(m_velocity.add(velocity));
    }
    public double getCollisionRadius() {
        return m_collisionRadius;
    }
    public double getDamp() {
        return m_damp;
    }
    public double getRadius() {
        return m_collisionRadius;
    }
    public Physics.CollideMask getCollisionType() {
        return m_collisionType;
    }
    public void setCollisionType(Physics.CollideMask type) {
        m_collisionType = type;
    }
    
    public EnumSet<Physics.CollideMask> collisionFlags;

    private Physics.CollideMask m_collisionType;
    private double m_damp;
    private double m_maxSpeed;
    private Point2D m_position;
    private Point2D m_velocity;
    private double m_collisionRadius;
    private int m_id;
    private static int m_idCounter = 0;
}
