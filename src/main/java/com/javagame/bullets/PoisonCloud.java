package com.javagame.bullets;

import javafx.geometry.Point2D;

import com.javagame.Animation;
import com.javagame.Sprite;

final public class PoisonCloud extends Bullet {
    public PoisonCloud(Point2D position, Point2D dir, double speed, int damage) {
        super(Sprite.get("poisoncloud.png", 64, false), damage, position.add(dir.multiply(0.2)), speed, 0.95,
            new Animation(0, 3, 1, 10, true, 1), new Animation(2, 3, 1, 10, false, 1));
        getRigidBody().setVelocity(dir.multiply(5.0));
        setVOffset(0.0);
    }
}
