package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

public abstract class Bullet extends Entity {
    public Bullet(Sprite sprite, int damage, Point2D position, double maxSpeed, double radius, Animation animFly, Animation animDie) {
        super(sprite, 0, damage, radius * 2, position, maxSpeed, radius, 1);
        m_hit = false;
        m_animFly = animFly;
        m_animDie = animDie;
        getRigidBody().setCollisionType(Physics.CollideMask.BULLET);
        getRigidBody().collisionFlags = EnumSet.of(
            Physics.CollideMask.WALL, Physics.CollideMask.ENEMY);
    }
    @Override public void update(Input input, double dt, World world) {
        if (m_hit) {
            m_animDie.step(dt);
            setAnimTileId(m_animDie.getFrame());
            if (m_animDie.isFinished()) {
                sheduleDestroy();                
            }
        } else {
            m_animFly.step(dt);
            setAnimTileId(m_animFly.getFrame());
        }
    }
    @Override public void onCollideWall() {
        m_hit = true;
        getRigidBody().setVelocity(new Point2D(0, 0));
    }
    @Override public void onCollideEntity(Entity ent) {
        m_hit = true;
        getRigidBody().setVelocity(new Point2D(0, 0));
        getRigidBody().collisionFlags = EnumSet.noneOf(Physics.CollideMask.class);
    }

    private boolean m_hit;
    private Animation m_animFly;
    private Animation m_animDie;
}
