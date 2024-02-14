package com.javagame;

import javafx.geometry.Point2D;
import java.util.EnumSet;

final public class Player extends Entity {
    public Player(double x, double y, World world) {
        super(null, 1000, 0, 0, new Point2D(x, y), 0.05, 0.2, 0.2);
        m_speed = 0.002;
        m_sensitivity = 0.05;
        m_healTimer = 0;
        m_dir = new Point2D(1, 0).normalize();
        getRigidBody().setCollisionType(Physics.CollideMask.PLAYER);
        getRigidBody().collisionFlags = EnumSet.of(
            Physics.CollideMask.WALL, Physics.CollideMask.ENEMY);
        m_world = world;
        m_guns = new Gun[] {
            new Gun(Sprite.get("pistol.png", 128, true), 0, new Animation(0, 6, 1, 15, false, 5), () -> {
                Bullet bullet = new PistolBullet(getPos(), m_dir.add(getRigidBody().getVelocity()), 0.05, 200);
                m_world.addEntity(bullet, true);
            }),
            new Gun(Sprite.get("chaingun.png", 128, true), 0, new Animation(0, 3, 1, 40, false, 1), () -> {
                Bullet bullet = new PistolBullet(getPos(), m_dir.add(getRigidBody().getVelocity()), 0.1, 400);
                m_world.addEntity(bullet, true);
            }),
            new Gun(Sprite.get("plasmagun.png", 128, true), 3, new Animation(0, 3, 1, 3, false, 2), () -> {
                Bullet bullet = new PlasmaBullet(getPos(), m_dir.add(getRigidBody().getVelocity()), 0.01, 800);
                m_world.addEntity(bullet, true);
            }),
        };
        m_currentGun = 0;
    }

    @Override public void update(Input input, double dt, World world) {
        m_world = world;
        m_healTimer += dt;
        if (m_healTimer >= 5000) {
            m_healTimer = 5000;
            setHealth(getHealth() + Math.min(1, (int)(0.5 * dt)));
        }

        // direction
        Point2D mousePos = input.getMousePos();
        if (m_mouseOld == null) {
            m_mouseOld = mousePos;
        }
        if (input.isHeld("MOUSE_SECONDARY")) {
            Point2D diff = mousePos.subtract(m_mouseOld).multiply(m_sensitivity);
            // dont care about Y axis (for now atleast)
            double angle = diff.getX() * m_sensitivity;
            m_dir = new Point2D(Math.cos(angle) * m_dir.getX() - Math.sin(angle) * m_dir.getY(),
                            Math.sin(angle) * m_dir.getX() + Math.cos(angle) * m_dir.getY());
        }
        m_mouseOld = mousePos;

        // position
        Point2D offset = new Point2D(0, 0);
        if (input.isHeld("W")) offset = offset.add(m_dir);
        if (input.isHeld("S")) offset = offset.subtract(m_dir);
        if (input.isHeld("A")) offset = offset.add(m_dir.getY(), -m_dir.getX());
        if (input.isHeld("D")) offset = offset.subtract(m_dir.getY(), -m_dir.getX());
        double multiplier = 1;
        if (input.isHeld("SHIFT")) multiplier = 3;

        if (offset.magnitude() > 0) {
            getRigidBody().addVelocity(offset.normalize().multiply(m_speed * multiplier * dt));
        }

        if (input.isPressed("DIGIT1")) {
            m_currentGun = 0;
        } else if (input.isPressed("DIGIT2")) {
            m_currentGun = 1;
        } else if (input.isPressed("DIGIT3")) {
            m_currentGun = 2;
        }

        // shooting
        for (int i = 0; i < m_guns.length; i++) {
            if (i == m_currentGun) {
                m_guns[i].update(dt);
            } else {
                m_guns[i].reset();
            }
        }
        if (input.isHeld("MOUSE_PRIMARY")) {
            m_guns[m_currentGun].shoot();
        }
    }
    public Point2D getPos() {
        return getRigidBody().getPosition();
    }
    public Point2D getDir() {
        return new Point2D(m_dir.getX(), m_dir.getY());
    }
    public void setPos(double x, double y) {
        getRigidBody().setPosition(new Point2D(x, y));
    }
    public void setDir(double x, double y) {
        m_dir = new Point2D(x, y).normalize();
    }
    public Sprite getGunSprite() {
        return m_guns[m_currentGun].getSprite();
    }
    public int getGunSpriteFrame() {
        return m_guns[m_currentGun].getSpriteFrame();
    }
    public void takeDamage(int damage) {
        setHealth(getHealth() - damage);
        m_healTimer = 0;
    }

    @Override public void onCollideWall() {}
    @Override public void onCollideEntity(Entity ent) {
        
    }
    
    private World m_world;
    private double m_healTimer;
    private int m_currentGun;
    private Gun[] m_guns;
    private Point2D m_dir;
    private double m_sensitivity;
    private double m_speed;
    private Point2D m_mouseOld;
}
