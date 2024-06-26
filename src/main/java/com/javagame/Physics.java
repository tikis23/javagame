package com.javagame;

import java.util.HashMap;
import java.util.HashSet;
import javafx.geometry.Point2D;
import com.javagame.bullets.Bullet;

final public class Physics {
    public enum CollideMask {
        PLAYER,
        WALL,
        BULLET,
        ENEMY_BULLET,
        ENEMY
    }

    public Physics(int[] map, int mapWidth, int mapHeight) {
        m_map = map;
        m_mapWidth = mapWidth;
        m_mapHeight = mapHeight;
        m_entitites = new HashMap<Integer, Entity>();
    }
    public void addEntity(Entity ent) {
        m_entitites.put(ent.getRigidBody().getId(), ent);
    }
    public void removeEntity(Entity ent) {
        m_entitites.remove(ent.getRigidBody().getId());
    }
    public void update() {
        HashMap<Entity, HashSet<Entity>> collidedEntities = new HashMap<>(); // used to prevent multiple onCollision calls
        for (Entity ent : m_entitites.values()) {
            RigidBody body = ent.getRigidBody();
            Point2D pos = body.getPosition();
            Point2D vel = body.getVelocity();
            double r = body.getCollisionRadius();

            // check if in bounds
            if ((int)pos.getX() < 0 || (int)pos.getX() >= m_mapWidth || 
                (int)pos.getY() < 0 || (int)pos.getY() >= m_mapHeight) continue;

            boolean collidingWithWall = false;
            if (body.collisionFlags.contains(CollideMask.WALL)) {
                Point2D rDir = new Point2D(vel.getX() > 0 ? r : -r, vel.getY() > 0 ? r : -r);
                Point2D nextPos = pos.add(vel.add(rDir));
                int gridX = (int)Math.floor(nextPos.getX());
                int gridY = (int)Math.floor(nextPos.getY());
                
                boolean xEmpty = false;
                if (gridX < 0 || gridX >= m_mapWidth) {
                    vel = vel.subtract(nextPos.getX() - (int)nextPos.getX(), 0);
                    collidingWithWall = true;
                } else if (m_map[(int)pos.getY() * m_mapWidth + gridX] != 0) {
                    vel = vel.subtract(nextPos.getX() - (int)nextPos.getX() - (vel.getX() > 0 ? 0 : 1), 0);
                    collidingWithWall = true;
                } else {
                    xEmpty = true;
                }
                if (gridY < 0 || gridY >= m_mapHeight) {
                    vel = vel.subtract(0, nextPos.getY() - (int)nextPos.getY());
                    collidingWithWall = true;
                } else if (m_map[gridY * m_mapWidth + (int)pos.getX()] != 0) {
                    vel = vel.subtract(0, nextPos.getY() - (int)nextPos.getY() - (vel.getY() > 0 ? 0 : 1));
                    collidingWithWall = true;
                } else if (xEmpty && m_map[gridY * m_mapWidth + gridX] != 0) { // corner case
                    collidingWithWall = true;
                    if (vel.getX() > vel.getY()) {
                        vel = vel.subtract(0, nextPos.getY() - (int)nextPos.getY() - (vel.getY() > 0 ? 0 : 1));
                    } else {
                        vel = vel.subtract(nextPos.getX() - (int)nextPos.getX() - (vel.getX() > 0 ? 0 : 1), 0);
                    }
                }
            }
            // collide with other bodies
            HashSet<Entity> collidedWith = new HashSet<>();
            for (Entity other : m_entitites.values()) {
                if (ent == other) continue;
                if (ent.ignoresCollisionWith(other) || other.ignoresCollisionWith(ent)) continue;
                if (ent instanceof Bullet && other instanceof Bullet) continue;

                RigidBody otherBody = other.getRigidBody();
                if (body.collisionFlags.contains(otherBody.getCollisionType()) && otherBody.collisionFlags.contains(body.getCollisionType())) {
                    double bodyDist = pos.add(vel).distance(otherBody.getPosition());
                    double minDist = r + otherBody.getCollisionRadius();
                    if (bodyDist < minDist) {
                        // if collision already exists dont add
                        HashSet<Entity> otherSet = collidedEntities.get(other); 
                        if (otherSet != null && !otherSet.contains(ent)) {
                            collidedWith.add(other);
                        }
                        // adjust velocity
                        if (ent instanceof Bullet || other instanceof Bullet) continue;
                        Point2D offsetDir = pos.add(vel).subtract(otherBody.getPosition()).normalize();
                        offsetDir = offsetDir.multiply(minDist - bodyDist);
                        vel = vel.add(offsetDir);
                    }
                }
            }
            collidedEntities.put(ent, collidedWith);

            body.setPosition(pos.add(vel));
            vel = vel.multiply(body.getDamp());
            body.setVelocity(vel);

            if (collidingWithWall) ent.onCollideWall();
        }
        for (Entity ent : collidedEntities.keySet()) {
            HashSet<Entity> colSet = collidedEntities.get(ent);
            for (Entity collidedEnt : colSet) {
                ent.onCollideEntity(collidedEnt);
                collidedEnt.onCollideEntity(ent);
            }
        }
    }

    private int[] m_map;
    private int m_mapWidth;
    private int m_mapHeight;
    private HashMap<Integer, Entity> m_entitites;
}
