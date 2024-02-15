package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

final public class Blob extends Enemy {
    public Blob(Point2D position) {
        super(Sprite.get("blob.png", 64, false), 2500, 200, 1.0, position, 0.5, 0.5, 0.5);
        setAggroRange(30, 80);
        m_walking = false;
        m_attacking = false;
        m_speed = 0.0005;
        m_attackRange = 1.0;
        m_path = new Pathfinding(400);
        m_attacked = false;

        m_animIdle = new Animation(0, 1, 1, 2, true, 1);
        m_animWalking = new Animation(0, 3, 1, 8, true, 1);
        m_animAttacking = new Animation(5, 6, 1, 4, false, 5);
        m_animTakeDamage = new Animation(10, 10, 1, 5, false, 1);
        m_animDying = new Animation(10, 14, 1, 5, false, 20);
    }
    @Override public void update(Input input, double dt, World world) {
        updateAggro(world, dt);
        RigidBody body = getRigidBody();

        if (getHealth() <= 0) {
            setDying(true);
            body.collisionFlags = EnumSet.noneOf(Physics.CollideMask.class);
        }

        if (isTakingDamage()) {
            m_animTakeDamage.step(dt);
            setAnimTileId(m_animTakeDamage.getFrame());
            if (m_animTakeDamage.isFinished()) {
                setTakingDamage(false);
                m_animTakeDamage.reset();
            }
        } else if (isDying()) {
            m_animDying.step(dt);
            setAnimTileId(m_animDying.getFrame());
            if (m_animDying.isFinished()) {
                sheduleDestroy();
            }
        } else {
            // ai
            if (isAggro()) {
                Point2D pos = getRigidBody().getPosition();
                double dist = world.player.getPos().distance(pos);
                if (dist < m_attackRange) {
                    m_attacking = true;
                } else {
                    m_path.update(world, body.getPosition(), world.player.getPos(), dt);
                    Point2D[] path = m_path.getPath();
                    m_walking = false;
                    if (path != null) {
                        m_walking = true;
                        double speed = m_speed * dt;
                        int startIndex = 0;
                        for (int i = 0; i < path.length; i++) {
                            if (body.getPosition().distance(path[i]) < 0.5) {
                                startIndex = i + 1;
                                break;
                            }
                        }
                        startIndex = Math.min(startIndex, path.length - 1);
                        Point2D from = path[startIndex];
                        if (startIndex + 1 < path.length) from = path[startIndex + 1];
                        Point2D diff = from.subtract(body.getPosition());
                        diff = diff.normalize().multiply(speed);
                        body.addVelocity(diff);
                    }
                }
            }

            if (m_attacking) {
                if (!m_attacked) {
                    world.player.takeDamage(getDamage());
                    m_attacked = true;
                }
                m_animAttacking.step(dt);
                setAnimTileId(m_animAttacking.getFrame());
                if (m_animAttacking.isFinished()) {
                    m_attacking = false;
                    m_attacked = false;
                    m_animAttacking.reset();
                }
            } else if (m_walking) {
                m_animWalking.step(dt);
                setAnimTileId(m_animWalking.getFrame());
            } else {
                m_animIdle.step(dt);
                setAnimTileId(m_animIdle.getFrame());
            }
        }
    }

    private boolean m_attacked;
    private Pathfinding m_path;
    private double m_attackRange;
    private double m_speed;
    private Animation m_animIdle;
    private boolean m_walking;
    private Animation m_animWalking;
    private boolean m_attacking;
    private Animation m_animAttacking;
    private Animation m_animTakeDamage;
    private Animation m_animDying;
}