package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

public abstract class Bullet extends Entity {
    public Bullet(Sprite sprite, int damage, Point2D position, double maxSpeed, double radius, Animation animFly, Animation animDie) {
        super(sprite, 0, damage, radius * 2, position, maxSpeed, radius, 1);
        m_hit = false;
        m_animFly = animFly;
        m_animDie = animDie;
        m_pierce = 1;
        m_acceleration = 1;
        m_travelDist = Double.MAX_VALUE;
        m_travelStartPos = position;
        getRigidBody().setCollisionType(Physics.CollideMask.BULLET);
        getRigidBody().collisionFlags = EnumSet.of(
            Physics.CollideMask.WALL, Physics.CollideMask.ENEMY);
    }
    public void setPierce(int pierce) {
        m_pierce = pierce;
    }
    public void setAcceleration(double acceleration) {
        m_acceleration = acceleration;
    }
    public void setTravelDistance(double travelDist) {
        m_travelDist = travelDist;
    }
    public void offsetFlyAnimation(double amount) {
        m_animFly.step(amount);
    }
    @Override public void update(Input input, double dt, World world) {
        if (m_hit) {
            m_animDie.step(dt);
            setAnimTileId(m_animDie.getFrame());
            if (m_animDie.isFinished()) {
                sheduleDestroy();                
            }
        } else {
            if (m_travelStartPos.distance(getRigidBody().getPosition()) >= m_travelDist) {
                getRigidBody().setVelocity(new Point2D(0, 0));
                m_hit = true;
            }
            getRigidBody().setVelocity(getRigidBody().getVelocity().multiply(m_acceleration));
            m_animFly.step(dt);
            setAnimTileId(m_animFly.getFrame());
        }
    }
    @Override public void onCollideWall() {
        m_hit = true;
        getRigidBody().setVelocity(new Point2D(0, 0));
    }
    @Override public void onCollideEntity(Entity ent) {
        if (m_pierce == -1) {
            
        } else {
            m_pierce--;
            if (m_pierce <= 0) {
                m_hit = true;
                getRigidBody().setVelocity(new Point2D(0, 0));
                getRigidBody().collisionFlags = EnumSet.noneOf(Physics.CollideMask.class);
            }
            ignoredCollisionAdd(ent);
        }
    }

    private Point2D m_travelStartPos;
    private double m_travelDist;
    private boolean m_hit;
    private Animation m_animFly;
    private Animation m_animDie;
    private int m_pierce;
    private double m_acceleration;
}
