package com.javagame;

import java.util.HashMap;
import javafx.geometry.Point2D;

final public class Physics {
    public Physics(int[] map, int mapWidth, int mapHeight) {
        m_map = map;
        m_mapWidth = mapWidth;
        m_mapHeight = mapHeight;
        m_bodies = new HashMap<Integer, RigidBody>();
    }
    public void addRigidBody(RigidBody body) {
        m_bodies.put(body.getId(), body);
    }
    public void removeRigidBody(RigidBody body) {
        m_bodies.remove(body.getId());
    }
    public void update() {
        for (RigidBody body : m_bodies.values()) {
            Point2D pos = body.getPosition();
            Point2D vel = body.getVelocity();
            double r = body.getCollisionRadius();

            Point2D rDir = new Point2D(vel.getX() > 0 ? r : -r, vel.getY() > 0 ? r : -r);
            Point2D nextPos = pos.add(vel.add(rDir));
            int gridX = (int)Math.floor(nextPos.getX());
            int gridY = (int)Math.floor(nextPos.getY());
            
            boolean xEmpty = false;
            if (gridX < 0 || gridX >= m_mapWidth) {
                vel = vel.subtract(nextPos.getX() - (int)nextPos.getX(), 0);
            } else if (m_map[(int)pos.getY() * m_mapWidth + gridX] > 0) {
                vel = vel.subtract(nextPos.getX() - (int)nextPos.getX() - (vel.getX() > 0 ? 0 : 1), 0);
            } else {
                xEmpty = true;
            }
            if (gridY < 0 || gridY >= m_mapHeight) {
                vel = vel.subtract(0, nextPos.getY() - (int)nextPos.getY());
            } else if (m_map[gridY * m_mapWidth + (int)pos.getX()] > 0) {
                vel = vel.subtract(0, nextPos.getY() - (int)nextPos.getY() - (vel.getY() > 0 ? 0 : 1));
            } else if (xEmpty && m_map[gridY * m_mapWidth + gridX] > 0) { // corner case
                if (vel.getX() > vel.getY()) {
                    vel = vel.subtract(0, nextPos.getY() - (int)nextPos.getY() - (vel.getY() > 0 ? 0 : 1));
                } else {
                    vel = vel.subtract(nextPos.getX() - (int)nextPos.getX() - (vel.getX() > 0 ? 0 : 1), 0);
                }
            }

            body.setPosition(pos.add(vel));
            vel = vel.multiply(body.getDamp());
            body.setVelocity(vel);
            // TODO: collide with other bodies
        }
    }

    private int[] m_map;
    private int m_mapWidth;
    private int m_mapHeight;
    private HashMap<Integer, RigidBody> m_bodies;
}
