package com.javagame.enemies;

import javafx.geometry.Point2D;
import java.util.EnumSet;

import com.javagame.Animation;
import com.javagame.Sprite;
import com.javagame.World;
import com.javagame.Input;
import com.javagame.Pathfinding;
import com.javagame.bullets.Bullet;
import com.javagame.bullets.FireballBullet;
import com.javagame.Gun;
import com.javagame.Physics;
import com.javagame.RigidBody;

final public class Demon extends Enemy {
    public Demon(Point2D position) {
        super(Sprite.get("demon.png", 64, false), 4000, 0, 0.9, position, 0.5, 0.45, 0.5);
        setAggroRange(50, 90);
        m_walking = false;
        m_attacking = false;
        m_speed = 0.0005;
        m_attackRange = 4;
        m_gun = null;
        m_path = new Pathfinding(600);

        m_animIdle = new Animation(5, 5, 1, 2, true, 1);
        m_animWalking = new Animation(0, 3, 1, 8, true, 1);
        m_animTakeDamage = new Animation(10, 10, 1, 5, false, 1);
        m_animDying = new Animation(10, 14, 1, 5, false, 20);
    }
    @Override public void update(Input input, double dt, World world) {
        if (m_gun == null) {
            m_gun = new Gun(null, 1, new Animation(5, 7, 1, 2, false, 5), () -> {
                int num_fires = 6;
                double bulletSpeed = 0.04;
                Point2D pos = getRigidBody().getPosition();
                Point2D targetPos = world.player.getPos();
                double damp = 1.0 / world.player.getRigidBody().getDamp();
                Point2D targetVel = world.player.getRigidBody().getVelocity().multiply(damp);
                
                targetPos = Pathfinding.predictImpactPosition(targetPos, targetVel, pos, bulletSpeed);
                Point2D shootDir = targetPos.subtract(pos).normalize();
                Point2D perpShootDir = new Point2D(-shootDir.getY(), shootDir.getX());
                for (int i = 0; i < num_fires; i++) {
                    double newBulletSpeed = bulletSpeed - bulletSpeed * (Math.random() *  0.9);
                    Point2D newShootDir = shootDir.add(perpShootDir.multiply(Math.random() * 0.5 - 0.25));
                    Bullet bullet = new FireballBullet(pos, newShootDir, newBulletSpeed, 2);
                    bullet.getRigidBody().setCollisionType(Physics.CollideMask.ENEMY_BULLET);
                    bullet.getRigidBody().collisionFlags = EnumSet.of(Physics.CollideMask.WALL, Physics.CollideMask.PLAYER);
                    bullet.setTravelDistance(m_attackRange * (Math.random() * 0.5 + 0.5));
                    bullet.setPierce(-1);
                    bullet.setAcceleration(1.15);
                    bullet.offsetFlyAnimation(Math.random() * 1000);
                    world.addEntity(bullet, true);
                }
            });
        }
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
                double dist = pos.distance(world.player.getPos());
                
                m_attacking = false;
                if (dist < m_attackRange) {
                    double hitDist = Pathfinding.castRay(world, pos, world.player.getPos().subtract(pos).normalize());
                    if (dist < hitDist) {
                        m_attacking = true;
                    }
                } 
                if (!m_attacking) {
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
                m_gun.update(dt);
                m_gun.shoot();
                setAnimTileId(m_gun.getSpriteFrame());
            } else if (m_walking) {
                m_animWalking.step(dt);
                setAnimTileId(m_animWalking.getFrame());
            } else {
                m_animIdle.step(dt);
                setAnimTileId(m_animIdle.getFrame());
            }
        }
    }

    private Pathfinding m_path;
    private double m_attackRange;
    private double m_speed;
    private Animation m_animIdle;
    private boolean m_walking;
    private Animation m_animWalking;
    private boolean m_attacking;
    private Gun m_gun;
    private Animation m_animTakeDamage;
    private Animation m_animDying;
}