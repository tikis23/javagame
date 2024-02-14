package com.javagame;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritablePixelFormat;
import java.nio.file.Paths;
import java.util.HashMap;

final public class Sprite {
    public static Sprite get(String name, int tileSize, boolean createImages) {
        if (m_sprites == null) m_sprites = new HashMap<>();
        if (m_sprites.containsKey(name)) {
            return m_sprites.get(name);
        }
        Sprite newSprite = loadSprite(name, tileSize, createImages);
        m_sprites.put(name, newSprite);
        return newSprite;
    }
    private static Sprite loadSprite(String name, int tileSize, boolean createImages) {
        Image img = new Image("file:" + Paths.get("sprites/" + name).toString());
        if (img == null) return null;
        int imgWidth = (int)img.getWidth();
        int imgHeight = (int)img.getHeight();
        int width = imgWidth / tileSize;
        int height = imgHeight / tileSize;
        if (createImages) {
            Image[] data = new Image[width * height];
            // divide into tiles and rotate
            int[] temp_imgbuff = new int[tileSize * tileSize];
            int[] temp_buff = new int[imgWidth * imgHeight];
            img.getPixelReader().getPixels(0, 0, imgWidth, imgHeight, WritablePixelFormat.getIntArgbInstance(), temp_buff, 0, imgWidth);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    for (int i = 0; i < tileSize; i++) {
                        for (int j = 0; j < tileSize; j++) {
                            int srcIndex = ((y * tileSize + i) * imgWidth + (x * tileSize + j));
                            int targetIndex = (j * tileSize + (tileSize - i - 1));
                            temp_imgbuff[targetIndex] = temp_buff[srcIndex];
                        }
                    }
                    WritableImage tileImg = new WritableImage(tileSize, tileSize);
                    tileImg.getPixelWriter().setPixels(0, 0, tileSize, tileSize, PixelFormat.getIntArgbInstance(), temp_imgbuff, 0, tileSize);
                    data[y * width + x] = tileImg;
                }
            }
            
            return new Sprite(null, data, tileSize, width, height);
        } else {
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
            return new Sprite(data, null, tileSize, width, height);
        }
    }
    private Sprite(byte[][] imageData, Image[] images, int tileSize, int imageWidth, int imageHeight) {
        m_byteImages = imageData;
        m_images = images;
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
        if (index >= m_byteImages.length) return null;
        return m_byteImages[index];
    }
    public Image getImageTile(int index) {
        if (index >= m_images.length) return null;
        return m_images[index];
    }

    private int m_tileSize;
    private int m_width;
    private int m_height;
    private Image[] m_images;
    private byte[][] m_byteImages;
    private static HashMap<String, Sprite> m_sprites;
}
