package com.javagame;

import org.json.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class WorldMap {
    public WorldMap(String mapName) {
        if (mapName == null) {
            createDefaultMap();
        } else {
            loadFromFile(mapName);
        }
    }
    public WorldMap(int mapWidth, int mapHeight, int[] mapData) {
        m_mapWidth = mapWidth;
        m_mapHeight = mapHeight;
        if (mapData == null) m_map = new int[m_mapWidth * m_mapHeight];
        else m_map = mapData;
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
        m_mapWidth = -1;
        m_mapHeight = -1;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(m_filePath + fileName + ".json"));
            JSONObject data = new JSONObject(new String(bytes));
            m_playerPosX = data.getDouble("playerPosX");
            m_playerPosY = data.getDouble("playerPosY");
            m_playerDirX = data.getDouble("playerDirX");
            m_playerDirY = data.getDouble("playerDirY");
            m_mapWidth = data.getInt("mapWidth");
            m_mapHeight = data.getInt("mapHeight");
            JSONArray jarray = data.getJSONArray("mapData");
            m_map = new int[jarray.length()];
            for (int i = 0; i < jarray.length(); i++) {
                m_map[i] = jarray.getInt(i);
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
        if (m_map == null || m_map.length < m_mapWidth * m_mapHeight || m_mapWidth < 1 || m_mapHeight < 1) {
            System.out.println("Failed to load file: " + m_filePath + fileName + ".json");
            createDefaultMap();
        }
    }
    public void saveToFile(String fileName) {
        JSONObject data = new JSONObject();
        data.put("mapData", m_map);
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

    private static String m_filePath = "maps/";
    private int m_mapWidth;
    private int m_mapHeight;
    private double m_playerPosX;
    private double m_playerPosY;
    private double m_playerDirX;
    private double m_playerDirY;
    private int[] m_map;
}
