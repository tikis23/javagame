package com.javagame.bullets;

import javafx.geometry.Point2D;

import com.javagame.Animation;
import com.javagame.Sprite;

final public class PistolBullet extends Bullet {
    public PistolBullet(Point2D position, Point2D dir, double speed, int damage) {
        super(Sprite.get("bullet1.png", 32, false), damage, position.add(dir.multiply(0.5)), speed, 0.1,
            new Animation(0, 1, 1, 10, true, 1), new Animation(2, 5, 1, 10, false, 1));
        getRigidBody().setVelocity(dir.multiply(5.0));
        setVOffset(0.25);
    }
}
