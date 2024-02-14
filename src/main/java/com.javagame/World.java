package com.javagame;

import javafx.geometry.Point2D;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;

final public class World {
    public World(String name, Player oldPlayer) {
        m_complete = false;
        m_finish = false;

        m_map = new WorldMap(name);
        m_exits = m_map.getExits();
        m_physics = new Physics(getMap(), getMapWidth(), getMapHeight());
        m_animTiles = new HashMap<>();
        m_entities = new ArrayList<>();
        m_addEntityQueue = new ArrayList<>();

        if (oldPlayer == null) {
            player = new Player(m_map.getPlayerPosX(), m_map.getPlayerPosY(), this);
        } else {
            player = oldPlayer;
            player.setPos(m_map.getPlayerPosX(), m_map.getPlayerPosY());
        }
        player.setDir(m_map.getPlayerDirX(), m_map.getPlayerDirY());
        addEntity(player, false);

        for (WorldMap.EnemyData enemy : m_map.getEnemies()) {
            Class<?> cl = WorldMap.enemyNames.get(enemy.name);
            if (cl == null) continue;
            try {
                Enemy en = (Enemy)cl.getConstructor(Point2D.class).newInstance(enemy.pos);
                addEntity(en, false);
            } catch(NoSuchMethodException | SecurityException | InstantiationException |
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                System.out.println("Failed to create enemy " + enemy.name);
            }
        }
    }
    public int[] getMap() {
        return m_map.getMap();
    }
    public int getMapWidth() {
        return m_map.getMapWidth();
    }
    public int getMapHeight() {
        return m_map.getMapHeight();
    }
    public void update(Input input, double dt) {
        m_physics.update();
        
        // update all entities
        for (Entity ent : m_entities) {
            ent.update(input, dt, this);
        }

        // delete sheduled entities
        for (int i = 0; i < m_entities.size(); i++) {
            if (m_entities.get(i).isSheduledForDestroy()) {
                removeEntity(i);
                i--;
            }
        }

        // add queued entities
        for (Entity ent : m_addEntityQueue) {
            addEntity(ent, false);
        }
        m_addEntityQueue.clear();

        // check if all enemies are dead
        boolean enemiesLeft = false;
        for (Entity ent : m_entities) {
            if (ent.getRigidBody().getCollisionType() == Physics.CollideMask.ENEMY) {
                enemiesLeft = true;
                break;
            }
        }
        if (!enemiesLeft) {
            m_complete = true;
        }

        if (m_complete) {
            // if player is close to any exit, start opening
            for (int i = 0; i < m_exits.length; i++) {
                int x = m_exits[i][0];
                int y = m_exits[i][1];
                double exitDist = player.getPos().distance(new Point2D(x + 0.5, y + 0.5));
                if (exitDist < 2.5) {
                    int tileIndex = y * m_map.getMapWidth() + x;
                    m_map.getMap()[tileIndex] = -1;
                    m_animTiles.put(tileIndex, 1.0);
                    m_exits[i][0] -= 2000;
                    m_exits[i][1] -= 2000;
                }
                exitDist = player.getPos().distance(new Point2D(x + 2000 + 0.5, y + 2000 + 0.5));
                if (exitDist < 0.5) {
                    m_finish = true;
                }
            }
            // play animation tiles
            Iterator<Integer> it = m_animTiles.keySet().iterator();
            while (it.hasNext()) {
                Integer key = it.next();
                Double val = m_animTiles.get(key);
                val -= 0.001 * dt;
                m_animTiles.put(key, val);
                if (val <= -1) {
                    m_map.getMap()[key] = 0;
                    it.remove();
                }
            }
        }
    }
    public Double getAnimTileHeight(int tileIndex) {
        return m_animTiles.get(tileIndex);
    }
    public boolean finish() {
        return m_finish;
    }
    public String getTargetMap() {
        return m_map.getTargetMap();
    }
    public void setComplete() {
        m_complete = true;
    }
    public ArrayList<Entity> getEntities() {
        return m_entities;
    }
    public void addEntity(Entity ent, boolean addLater) {
        if (addLater) {
            m_addEntityQueue.add(ent);
        } else {
            m_entities.add(ent);
            m_physics.addEntity(ent);
        }
    }
    public void removeEntity(int index) {
        m_physics.removeEntity(m_entities.get(index));
        m_entities.remove(index);
    }

    public Player player;

    private ArrayList<Entity> m_addEntityQueue;
    private ArrayList<Entity> m_entities;
    private HashMap<Integer, Double> m_animTiles;
    private int[][] m_exits;
    private boolean m_finish;
    private boolean m_complete;
    private Physics m_physics;
    private WorldMap m_map;
}
