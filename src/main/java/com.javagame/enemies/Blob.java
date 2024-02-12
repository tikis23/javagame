package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

final public class Blob extends Entity {
    public Blob(Point2D position) {
        super(Sprite.get("blob.png", 64), 1000, 200, 1.0, position, 0.5, 0.5, 0.5);
        getRigidBody().setCollisionType(Physics.CollideMask.ENEMY);
        getRigidBody().collisionFlags = EnumSet.of(
            Physics.CollideMask.PLAYER, Physics.CollideMask.WALL, Physics.CollideMask.BULLET);

        m_walking = true;
        m_attacking = false;
        m_takingDamage = false;
        m_dying = false;

        m_animWalking = new Animation(0, 3, 1, 5, true, 1);
        m_animAttacking = new Animation(5, 6, 1, 4, false, 1);
        m_animTakeDamage = new Animation(10, 11, 1, 5, false, 1);
        m_animDying = new Animation(10, 14, 1, 5, false, 20);
    }
    @Override public void update(Input input, double dt, World world) {
        if (getHealth() <= 0) {
            m_dying = true;
            getRigidBody().collisionFlags = EnumSet.noneOf(Physics.CollideMask.class);
        }
        if (m_takingDamage) {
            m_animTakeDamage.step(dt);
            setAnimTileId(m_animTakeDamage.getFrame());
            if (m_animTakeDamage.isFinished()) {
                m_takingDamage = false;
                m_animTakeDamage.reset();
            }
        } else if (m_dying) {
            m_animDying.step(dt);
            setAnimTileId(m_animDying.getFrame());
            if (m_animDying.isFinished()) {
                sheduleDestroy();
            }
        } else {
            Point2D pos = getRigidBody().getPosition();
            double dist = world.player.getPos().distance(pos);
            if (dist < 1.2) {
                m_attacking = true;
            }

            if (m_attacking) {
                m_animAttacking.step(dt);
                setAnimTileId(m_animAttacking.getFrame());
                if (m_animAttacking.isFinished()) {
                    m_attacking = false;
                    m_animAttacking.reset();
                }
            } else if (m_walking) {
                m_animWalking.step(dt);
                setAnimTileId(m_animWalking.getFrame());
            }
        }
    }
    @Override public void onCollideWall() {
        
    }
    @Override public void onCollideEntity(Entity ent) {
        int damage = ent.getDamage();
        if (damage == 0) return;
        setHealth(getHealth() - damage);
        m_takingDamage = true;
    }

    private boolean m_walking;
    private Animation m_animWalking;
    private boolean m_attacking;
    private Animation m_animAttacking;
    private boolean m_takingDamage;
    private Animation m_animTakeDamage;
    private boolean m_dying;
    private Animation m_animDying;
}