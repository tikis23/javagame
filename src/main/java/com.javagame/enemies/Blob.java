package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

final public class Blob extends Entity {
    public Blob(Point2D position) {
        super(Sprite.get("blob.png", 64), 1.0, position, 0.5, 0.5, 0.5);
        getRigidBody().setCollisionType(Physics.CollideMask.ENEMY);
        getRigidBody().collisionFlags = EnumSet.of(
            Physics.CollideMask.PLAYER, Physics.CollideMask.WALL, Physics.CollideMask.BULLET);

        m_walking = true;
        m_attacking = false;
        m_dying = false;

        m_animWalking = new Animation(0, 3, 1, 5, true);
        m_animDying = new Animation(10, 30, 1, 5, false); // end frame out of bounds to pause on last frame
    }
    @Override public void update(Input input, double dt, World world) {
        if (m_dying) {
            m_animDying.step(dt);
            int frame = m_animDying.getFrame();
            if (frame > 14) frame = 14;
            setAnimTileId(frame);
            if (m_animDying.isFinished()) {
                sheduleDestroy();
            }
        } else if (m_walking) {
            m_animWalking.step(dt);
            setAnimTileId(m_animWalking.getFrame());
        }
    }
    @Override public void onCollideWall() {
        
    }
    @Override public void onCollideEntity(Entity ent) {
        if (ent instanceof Bullet) {
            m_dying = true;
            getRigidBody().collisionFlags = EnumSet.noneOf(Physics.CollideMask.class);
        }
    }

    private boolean m_walking;
    private Animation m_animWalking;
    private boolean m_attacking;
    private Animation m_animAttacking;
    private boolean m_dying;
    private Animation m_animDying;
}