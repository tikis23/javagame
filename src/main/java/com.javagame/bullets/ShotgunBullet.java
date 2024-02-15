package com.javagame;

import javafx.geometry.Point2D;

final public class ShotgunBullet extends Bullet {
    public ShotgunBullet(Point2D position, Point2D dir, double speed, int damage) {
        super(Sprite.get("bullet2.png", 32, false), damage, position.add(dir.multiply(0.5)), speed, 0.1,
            new Animation(0, 1, 1, 10, true, 1), new Animation(2, 5, 1, 10, false, 1));
        getRigidBody().setVelocity(dir.multiply(5.0));
        setVOffset(0.25);
    }
}
