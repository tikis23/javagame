package com.javagame;

import javafx.geometry.Point2D;

final public class FireballBullet extends Bullet {
    public FireballBullet(Point2D position, Point2D dir, double speed, int damage) {
        super(Sprite.get("fireball.png", 64, false), damage, position.add(dir.multiply(0.2)), speed, 0.45,
            new Animation(0, 3, 1, 8, true, 1), new Animation(2, 3, 1, 10, false, 1));
        getRigidBody().setVelocity(dir.multiply(5.0));
        setVOffset(0.05);
    }
}
