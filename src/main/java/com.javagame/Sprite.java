package com.javagame;

import javafx.scene.image.Image;
import javafx.scene.image.WritablePixelFormat;
import java.nio.file.Paths;
import java.util.HashMap;

final public class Sprite {
    public static Sprite get(String name, int tileSize) {
        if (m_sprites == null) m_sprites = new HashMap<>();
        if (m_sprites.containsKey(name)) {
            return m_sprites.get(name);
        }
        Sprite newSprite = loadSprite(name, tileSize);
        m_sprites.put(name, newSprite);
        return newSprite;
    }
    private static Sprite loadSprite(String name, int tileSize) {
        Image img = new Image("file:" + Paths.get("sprites/" + name).toString());
        if (img == null) return null;
        int imgWidth = (int)img.getWidth();
        int imgHeight = (int)img.getHeight();
        int width = imgWidth / tileSize;
        int height = imgHeight / tileSize;
        byte[][] data = new byte[width * height][tileSize * tileSize * 4];

        byte[] temp_buff = new byte[imgWidth * imgHeight * 4];
        img.getPixelReader().getPixels(0, 0, imgWidth, imgHeight, WritablePixelFormat.getByteBgraInstance(), temp_buff, 0, imgWidth * 4);
        // divide into tiles, rotate and convert bgra -> rgba
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int i = 0; i < tileSize; i++) {
                    for (int j = 0; j < tileSize; j++) {
                        int srcIndex = 4 * ((y * tileSize + i) * imgWidth + (x * tileSize + j));
                        int targetIndex = 4 * (j * tileSize + (tileSize - i - 1));
                        data[y * width + x][targetIndex + 0] = temp_buff[srcIndex + 2];
                        data[y * width + x][targetIndex + 1] = temp_buff[srcIndex + 1];
                        data[y * width + x][targetIndex + 2] = temp_buff[srcIndex + 0];
                        data[y * width + x][targetIndex + 3] = temp_buff[srcIndex + 3];
                    }
                }
            }
        }
        return new Sprite(data, tileSize, width, height);
    }
    private Sprite(byte[][] imageData, int tileSize, int imageWidth, int imageHeight) {
        m_image = imageData;
        m_tileSize = tileSize;
        m_width = imageWidth;
        m_height = imageHeight;
    }

    public int getTileSize() {
        return m_tileSize;
    }
    public int getWidth() {
        return m_width;
    }
    public int getHeight() {
        return m_height;
    }
    public byte[] getTile(int index) {
        if (index >= m_image.length) return null;
        return m_image[index];
    }

    private int m_tileSize;
    private int m_width;
    private int m_height;
    private byte[][] m_image;
    private static HashMap<String, Sprite> m_sprites;
}
