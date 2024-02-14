package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

public abstract class Enemy extends Entity {
    public Enemy(Sprite sprite, int maxHealth, int damage, double height, Point2D position, double maxSpeed, double radius, double velocityDamp) {
        super(sprite, maxHealth, damage, height, position, maxSpeed, radius, velocityDamp);
        getRigidBody().setCollisionType(Physics.CollideMask.ENEMY);
        getRigidBody().collisionFlags = EnumSet.of(Physics.CollideMask.PLAYER, Physics.CollideMask.ENEMY,
                    Physics.CollideMask.WALL, Physics.CollideMask.BULLET);
        m_takingDamage = false;
        m_dying = false;
    }
    public boolean isTakingDamage() {
        return m_takingDamage;
    }
    public void setTakingDamage(boolean takingDamage) {
        m_takingDamage = takingDamage;
    }
    public boolean isDying() {
        return m_dying;
    }
    public void setDying(boolean dying) {
        m_dying = dying;
    }
    @Override public void onCollideWall() {}
    @Override public void onCollideEntity(Entity ent) {
        if (ent.getRigidBody().getCollisionType() == Physics.CollideMask.ENEMY) return;
        int damage = ent.getDamage();
        if (damage == 0) return;
        setHealth(getHealth() - damage);
        m_takingDamage = true;
    }

    private boolean m_takingDamage;
    private boolean m_dying;
}