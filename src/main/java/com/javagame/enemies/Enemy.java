package com.javagame.enemies;

import javafx.geometry.Point2D;
import java.util.EnumSet;

import com.javagame.Entity;
import com.javagame.Physics;
import com.javagame.Sprite;
import com.javagame.World;
import com.javagame.Pathfinding;

public abstract class Enemy extends Entity {
    public Enemy(Sprite sprite, int maxHealth, int damage, double height, Point2D position, double maxSpeed, double radius, double velocityDamp) {
        super(sprite, maxHealth, damage, height, position, maxSpeed, radius, velocityDamp);
        getRigidBody().setCollisionType(Physics.CollideMask.ENEMY);
        getRigidBody().collisionFlags = EnumSet.of(Physics.CollideMask.PLAYER, Physics.CollideMask.ENEMY,
                    Physics.CollideMask.WALL, Physics.CollideMask.BULLET);
        m_takingDamage = false;
        m_dying = false;
        m_aggroFrom = 0;
        m_loseAggroFrom = 0;
        m_isAggro = false;
        m_aggroTimer = Math.random() * 2000 + 2000; // just to spread updateAggro calls a little across frames
    }
    public void setAggroRange(double aggroFrom, double loseAggroFrom) {
        m_aggroFrom = aggroFrom;
        m_loseAggroFrom = loseAggroFrom;
    }
    public void updateAggro(World world, double dt) {
        m_aggroTimer += dt;
        if (m_isAggro) {
            if (m_aggroTimer < 5000) return;
        } else {
            if (m_aggroTimer < 500) return;
        }
        m_aggroTimer = 0;
        
        Point2D pos = getRigidBody().getPosition();
        double dist = pos.distance(world.player.getPos());
        if (dist >= m_loseAggroFrom) {
            m_isAggro = false;
        } else if (dist <= m_aggroFrom) {
            double hitDist = Pathfinding.castRay(world, pos, world.player.getPos().subtract(pos).normalize());
            if (dist < hitDist) m_isAggro = true;
        }
            
    }
    public boolean isAggro() {
        return m_isAggro;
    }
    public boolean isTakingDamage() {
        return m_takingDamage;
    }
    public void setTakingDamage(boolean takingDamage) {
        m_takingDamage = takingDamage;
    }
    public boolean isDying() {
        return m_dying;
    }
    public void setDying(boolean dying) {
        m_dying = dying;
    }
    @Override public void onCollideWall() {}
    @Override public void onCollideEntity(Entity ent) {
        if (ent.getRigidBody().getCollisionType() == Physics.CollideMask.ENEMY) return;
        int damage = ent.getDamage();
        if (damage == 0) return;
        setHealth(getHealth() - damage);
        m_takingDamage = true;
    }

    private double m_aggroTimer;
    private double m_aggroFrom;
    private double m_loseAggroFrom;
    private boolean m_isAggro;
    private boolean m_takingDamage;
    private boolean m_dying;
}