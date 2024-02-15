package com.javagame;

import javafx.geometry.Point2D;
import java.util.HashSet;

public abstract class Entity {
    public Entity(Sprite sprite, int maxHealth, int damage, double height, Point2D position, double maxSpeed, double radius, double velocityDamp) {
        m_body = new RigidBody(position, maxSpeed, radius, velocityDamp);
        m_sprite = sprite;
        m_height = height;
        m_destroy = false;
        m_vOffset = 0;
        m_animTileId = 0;
        m_maxHealth = maxHealth;
        m_health = m_maxHealth;
        m_damage = damage;
        m_ignoredCollisions = new HashSet<>();
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
    public void setHealth(int health) {
        m_health = Math.min(m_maxHealth, Math.max(0, health));
    }
    public int getHealth() {
        return m_health;
    }
    public int getMaxHealth() {
        return m_maxHealth;
    }
    public void setDamage(int damage) {
        m_damage = damage;
    }
    public int getDamage() {
        return m_damage;
    }
    public boolean ignoresCollisionWith(Entity ent) {
        return m_ignoredCollisions.contains(ent);
    }
    public void ignoredCollisionAdd(Entity ent) {
        m_ignoredCollisions.add(ent);
    }
    public void ignoredCollisionRemove(Entity ent) {
        m_ignoredCollisions.remove(ent);
    }
 
    public abstract void update(Input input, double dt, World world);
    public abstract void onCollideWall();
    public abstract void onCollideEntity(Entity ent);

    private HashSet<Entity> m_ignoredCollisions;

    private int m_maxHealth;
    private int m_health;
    private int m_damage;

    private int m_animTileId;
    private double m_vOffset;
    private boolean m_destroy;
    private double m_height;
    private Sprite m_sprite;
    private RigidBody m_body;
}
