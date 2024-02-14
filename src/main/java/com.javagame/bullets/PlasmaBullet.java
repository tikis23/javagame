package com.javagame;

import javafx.geometry.Point2D;

final public class PlasmaBullet extends Bullet {
    public PlasmaBullet(Point2D position, Point2D dir, double speed, int damage) {
        super(Sprite.get("plasmaball.png", 64, false), damage, position.add(dir.multiply(0.5)), speed, 0.35,
            new Animation(0, 3, 1, 10, true, 1), new Animation(0, 3, 1, 10, false, 1));
        getRigidBody().setVelocity(dir.multiply(5.0));
        setVOffset(0.1);
    }
}
