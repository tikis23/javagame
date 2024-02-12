package com.javagame;

import javafx.geometry.Point2D;

public abstract class Entity {
    public Entity(Sprite sprite, double height, Point2D position, double maxSpeed, double radius, double velocityDamp) {
        m_body = new RigidBody(position, maxSpeed, radius, velocityDamp);
        m_sprite = sprite;
        m_height = height;
        m_destroy = false;
        m_vOffset = 0;
        m_animTileId = 0;
    }
    public RigidBody getRigidBody() {
        return m_body;
    }
    public Sprite getSprite() {
        return m_sprite;
    }
    public double getHeight() {
        return m_height;
    }
    public void sheduleDestroy() {
        m_destroy = true;
    }
    public boolean isSheduledForDestroy() {
        return m_destroy;
    }
    public double getVOffset() {
        return m_vOffset;
    }
    public void setVOffset(double offset) {
        m_vOffset = offset;
    }
    public int getAnimTileId() {
        return m_animTileId;
    }
    public void setAnimTileId(int animTileId) {
        m_animTileId = animTileId;
    }
    public abstract void update(Input input, double dt, World world);
    public abstract void onCollideWall();
    public abstract void onCollideEntity(Entity ent);

    private int m_animTileId;
    private double m_vOffset;
    private boolean m_destroy;
    private double m_height;
    private Sprite m_sprite;
    private RigidBody m_body;
}
