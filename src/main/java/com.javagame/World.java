package com.javagame;

final public class World {
    public World(String name) {
        if ("hub".equals(name)) {
            m_map = new WorldMap(10, 10, new int[]{
                2,2,2,2,1,1,2,2,2,2,2,0,0,0,0,0,0,0,0,2,
                2,0,1,0,0,0,0,1,0,2,2,0,0,0,0,0,0,0,0,2,
                2,0,1,0,0,0,0,1,0,2,2,0,1,0,0,0,0,1,0,2,
                2,0,0,0,0,0,0,0,0,2,2,0,1,0,1,1,0,1,0,2,
                2,0,0,0,0,0,0,0,0,2,2,2,2,2,2,2,2,2,2,2
            });
            player = new Player(5, 6);
            player.setDir(0, -1);
        } else {
            m_map = new WorldMap(name);
            player = new Player(m_map.getPlayerPosX(), m_map.getPlayerPosY());
            player.setDir(m_map.getPlayerDirX(), m_map.getPlayerDirY());
        }
        
        m_physics = new Physics(getMap(), getMapWidth(), getMapHeight());
        m_physics.addRigidBody(player.getRigidBody());
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
        player.update(input, dt);
        m_physics.update();
    }
    public Player player;

    private Physics m_physics;
    private WorldMap m_map;
}
