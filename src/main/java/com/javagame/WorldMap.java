package com.javagame;

import org.json.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import javafx.geometry.Point2D;

import com.javagame.enemies.*;

public class WorldMap {
    public static final HashMap<String, Class<?>> enemyNames = new HashMap<>();
    static {
        enemyNames.put("Blob", Blob.class);
        enemyNames.put("Hobo", Hobo.class);
        enemyNames.put("Gunblin", Gunblin.class);
        enemyNames.put("Demon", Demon.class);
    }
 
    public WorldMap(String mapName) {
        if (mapName == null) {
            createDefaultMap();
        } else if ("hub".equals(mapName)) {
            createDefaultMap();
            m_mapWidth = 10;
            m_mapHeight = 10;
            m_map = new int[] {
                2,2,2,2,1,1,2,2,2,2,2,0,0,0,0,0,0,0,0,2,
                2,0,3,0,0,0,0,3,0,2,2,0,0,0,0,0,0,0,0,2,
                2,0,3,0,0,0,0,3,0,2,2,0,3,0,0,0,0,3,0,2,
                2,0,0,0,0,0,0,0,0,2,2,0,3,0,3,3,0,3,0,2,
                2,0,0,0,0,0,0,0,0,2,2,2,2,2,2,2,2,2,2,2
            };
            m_exits = new int[][] {{4, 0}, {5, 0}};
            m_targetMap = "level1";
            setPlayerPos(5, 6);
            setPlayerDir(0, -1);
        } else {
            loadFromFile(mapName);
        }
    }
    public int[] getMap() {
        return m_map;
    }
    public int getMapWidth() {
        return m_mapWidth;
    }
    public int getMapHeight() {
        return m_mapHeight;
    }
    public void loadFromFile(String fileName) {
        m_map = null;
        m_enemies = new EnemyData[0];
        m_targetMap = "";
        m_exits = null;
        m_mapWidth = -1;
        m_mapHeight = -1;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(m_filePath + fileName + ".json"));
            JSONObject data = new JSONObject(new String(bytes));
            m_playerPosX = data.getDouble("playerPosX");
            m_playerPosY = data.getDouble("playerPosY");
            m_playerDirX = data.getDouble("playerDirX");
            m_playerDirY = data.getDouble("playerDirY");
            m_targetMap = data.optString("targetMap");
            m_mapWidth = data.getInt("mapWidth");
            m_mapHeight = data.getInt("mapHeight");
            JSONArray jarray = data.getJSONArray("mapData");
            m_map = new int[jarray.length()];
            for (int i = 0; i < jarray.length(); i++) {
                m_map[i] = jarray.getInt(i);
            }
            jarray = data.getJSONArray("exits");
            m_exits = new int[jarray.length()][2];
            for (int i = 0; i < jarray.length(); i++) {
                JSONArray subarray = jarray.getJSONArray(i);
                m_exits[i][0] = subarray.getInt(0);
                m_exits[i][1] = subarray.getInt(1);
            }
            jarray = data.getJSONArray("enemies");
            m_enemies = new EnemyData[jarray.length()];
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject obj = jarray.getJSONObject(i);
                m_enemies[i] = new EnemyData(obj.getString("name"), new Point2D(obj.getDouble("x"), obj.getDouble("y")));
            }
        } catch (IOException e) {
            System.out.println("Failed to load file: " + m_filePath + fileName + ".json");
            createDefaultMap();
            return;
        } catch (JSONException e) {
            System.out.println("Failed to load file: " + m_filePath + fileName + ".json");
            createDefaultMap();
            return;
        }
        // use an empty map to avoid errors
        if (m_enemies == null || m_map == null || m_exits == null || m_map.length < m_mapWidth * m_mapHeight ||
            m_mapWidth < 1 || m_mapHeight < 1) {
            System.out.println("Failed to load file: " + m_filePath + fileName + ".json");
            createDefaultMap();
        }
    }
    public void saveToFile(String fileName) {
        JSONObject data = new JSONObject();

        JSONArray enemiesJson = new JSONArray();
        for (EnemyData enemyData : m_enemies) {
            JSONObject enemy = new JSONObject().put("name", enemyData.name).put("x", enemyData.pos.getX()).put("y", enemyData.pos.getY());
            enemiesJson.put(enemy);
        }
        data.put("enemies", enemiesJson);
        data.put("mapData", m_map);
        data.put("exits", m_exits);
        data.put("targetMap", m_targetMap);
        data.put("mapWidth", m_mapWidth);
        data.put("mapHeight", m_mapHeight);
        data.put("playerPosX", m_playerPosX);
        data.put("playerPosY", m_playerPosY);
        data.put("playerDirX", m_playerDirX);
        data.put("playerDirY", m_playerDirY);

        try {
            Files.createDirectories(Paths.get(m_filePath));
            Files.write(Paths.get(m_filePath + fileName + ".json"), data.toString().getBytes());
        } catch (IOException e) {
            System.out.println("Failed to save file: " + m_filePath + fileName + ".json");
        }
    }
    public void resize(int newWidth, int newHeight) {
        if (newWidth < 1 || newHeight < 1) return;
        
        int[] newData = new int[newWidth * newHeight];
        for (int j = 0; j < newHeight; j++) {
            for (int i = 0; i < newWidth; i++) {
                if (i < m_mapWidth && j < m_mapHeight) {
                    newData[j * newWidth + i] = m_map[j * m_mapWidth + i];
                } else {
                    newData[j * newWidth + i] = 0;
                }
            }
        }
        m_map = newData;
        m_mapWidth = newWidth;
        m_mapHeight = newHeight;
    }
    public void createDefaultMap() {
        m_mapWidth = 2;
        m_mapHeight = 2;
        m_map = new int[]{0,0,0,0};
        m_exits = new int[][] {{-100000, -10000}};
        m_enemies = new EnemyData[0];
        m_targetMap = "start";
        setPlayerPos(1, 1);
        setPlayerDir(1, 0);
    }
    public void setPlayerPos(double x, double y) {
        m_playerPosX = x;
        m_playerPosY = y;
    }
    public double getPlayerPosX() {
        return m_playerPosX;
    }
    public double getPlayerPosY() {
        return m_playerPosY;
    }
    public void setPlayerDir(double x, double y) {
        m_playerDirX = x;
        m_playerDirY = y;
    }
    public double getPlayerDirX() {
        return m_playerDirX;
    }
    public double getPlayerDirY() {
        return m_playerDirY;
    }
    public void setTargetMap(String target) {
        m_targetMap = target;
    }
    public String getTargetMap() {
        return m_targetMap;
    }
    public void setExits(int[][] exits) {
        m_exits = exits;
    }
    public int[][] getExits() {
        return m_exits;
    }
    public EnemyData[] getEnemies() {
        return m_enemies;
    }
    public void setEnemies(EnemyData[] enemies) {
        m_enemies = enemies;
    }

    public static final class EnemyData {
        public EnemyData(String name, Point2D pos) {
            this.name = name;
            this.pos = pos;
        }
        String name;
        Point2D pos;
    }

    private EnemyData[] m_enemies;
    private String m_targetMap;
    private static String m_filePath = "maps/";
    private int[][] m_exits;
    private int m_mapWidth;
    private int m_mapHeight;
    private double m_playerPosX;
    private double m_playerPosY;
    private double m_playerDirX;
    private double m_playerDirY;
    private int[] m_map;
}
