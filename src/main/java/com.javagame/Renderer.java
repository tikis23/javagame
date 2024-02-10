package com.javagame;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.*;
import javafx.geometry.Point2D;
import java.nio.file.Paths;

final public class Renderer {
    public Renderer(Group group, int width, int height) {
        m_width = width;
        m_height = height;
        m_canvas = new Canvas(m_width, m_height);
        group.getChildren().add(m_canvas);
        m_gc = m_canvas.getGraphicsContext2D();
        m_gc.rotate(-90);
        m_image = new WritableImage(m_height, m_width);
        m_buffer = new byte[m_width*m_height*3];

        // load textures
        m_textureSize = 64;
        m_textures = new byte[2][m_textureSize*m_textureSize*3];
        byte[] temp_buff = new byte[m_textureSize*m_textureSize*4];
        for (int t = 0; t < m_textures.length; t++) {
            Image img = new Image("file:" + Paths.get("textures/" + (t + 1) + ".jpg").toString());
            img.getPixelReader().getPixels(0, 0, m_textureSize, m_textureSize,
                        WritablePixelFormat.getByteBgraInstance(), temp_buff, 0, m_textureSize*4);
            // copy rgb only and rotate
            for (int i = 0; i < m_textureSize; i++) {
                for (int j = 0; j < m_textureSize; j++) {
                    m_textures[t][(i * m_textureSize + (m_textureSize - j - 1)) * 3 + 0] = temp_buff[(j * m_textureSize + i) * 4 + 2];
                    m_textures[t][(i * m_textureSize + (m_textureSize - j - 1)) * 3 + 1] = temp_buff[(j * m_textureSize + i) * 4 + 1];
                    m_textures[t][(i * m_textureSize + (m_textureSize - j - 1)) * 3 + 2] = temp_buff[(j * m_textureSize + i) * 4 + 0];
                }
            }
        }
    }
    public void resize(int x, int y) {
        m_width = x;
        m_height = y;
        m_canvas.setWidth(x);
        m_canvas.setHeight(y);
        m_image = new WritableImage(m_height, m_width);
        m_buffer = new byte[m_width*m_height*3];
    }
    public void render(World world) {
        PixelWriter writer = m_image.getPixelWriter();
        
        int[] map = world.getMap();
        // get perpendicular vector to dir, used to get rayDir
        Point2D perpDir = new Point2D(-world.player.getDir().getY(), world.player.getDir().getX());
        for (int i = 0; i < m_width; i++) {
            // convert [0, width) to [-1, 1]
            double dirOffset = i / (double)m_width * 2.0 - 1.0;
            dirOffset *= 0.6; // decrease FOV
            dirOffset *= (double)m_width / m_height; // correct aspect ratio
            Point2D rayOrigin = world.player.getPos();
            Point2D rayDir = world.player.getDir().add(perpDir.multiply(dirOffset)); // dont normalize to remove fish-eye

            boolean hit = false;
            int material = 0;
            // setup dda
            int gridX = (int)rayOrigin.getX();
            int gridY = (int)rayOrigin.getY();
            Point2D deltaDist = new Point2D(rayDir.getX() == 0 ? Double.MAX_VALUE : Math.abs(1.0 / rayDir.getX()),
                                            rayDir.getY() == 0 ? Double.MAX_VALUE : Math.abs(1.0 / rayDir.getY()));
            int rayStepX = rayDir.getX() > 0 ? 1 : -1;
            int rayStepY = rayDir.getY() > 0 ? 1 : -1;
            Point2D sideDist = new Point2D(
                (rayStepX * ((double)gridX - rayOrigin.getX()) + (double)rayStepX * 0.5 + 0.5) * deltaDist.getX(),
                (rayStepY * ((double)gridY - rayOrigin.getY()) + (double)rayStepY * 0.5 + 0.5) * deltaDist.getY()
            );
            int hitDir = 1;
            // raycast
            while (gridX >= 0 && gridY >= 0 && gridX < world.getMapWidth() && gridY < world.getMapHeight()) {
                int tile = map[gridY * world.getMapWidth() + gridX];
                if (tile > 0) {
                    material = tile;
                    hit = true;
                    break;
                }
                if (sideDist.getX() < sideDist.getY()) {
                    sideDist = sideDist.add(deltaDist.getX(), 0);
                    gridX += rayStepX;
                    hitDir = 1;
                } else {
                    sideDist = sideDist.add(0, deltaDist.getY());
                    gridY += rayStepY;
                    hitDir = 2;
                }
            }
            // draw line
            double hitDist = hitDir == 1 ? sideDist.getX() - deltaDist.getX() : sideDist.getY() - deltaDist.getY();
            if (hitDist <= 0) hitDist = 1;
            double texX = 0;
            if (hitDir == 1) {
                texX = rayOrigin.add(rayDir.multiply(hitDist)).getY();
            } else {
                texX = rayOrigin.add(rayDir.multiply(hitDist)).getX();
            }
            texX -= Math.floor(texX);
            texX *= m_textureSize;
            if (hit) {
                fillLine(i, (int)texX, 1.0 / hitDist, hitDir, material - 1);
            } else {
                fillLine(i, (int)texX, 1.0 / hitDist, hitDir, -1);
            }
        }

        writer.setPixels(0, 0, m_height, m_width, PixelFormat.getByteRgbInstance(), m_buffer, 0, m_height*3);
        m_gc.drawImage(m_image, -m_height, 0);
    }
    private void fillLine(int x, int texX, double h, int side, int textureId) {
        int height = (int)(m_height * h * 0.5);
        int mid = m_height / 2;
        int low = Math.max(0, mid - height);
        int high = Math.min(m_height, mid + height);
        
        for (int i = x * m_height * 3; i < (x * m_height + low) * 3; i += 3) {
            m_buffer[i + 0] = (byte)80;
            m_buffer[i + 1] = (byte)40;
            m_buffer[i + 2] = (byte)0;
        }
        if (textureId == -1) {
            for (int i = (x * m_height + low) * 3; i < (x * m_height + high) * 3; i += 3) {
                m_buffer[i + 0] = (byte)(60 * (Math.min(1.0, side * 0.7)));
                m_buffer[i + 1] = (byte)(60 * (Math.min(1.0, side * 0.7)));
                m_buffer[i + 2] = (byte)(60 * (Math.min(1.0, side * 0.7)));
            }
        } else {
            for (int i = (x * m_height + low) * 3; i < (x * m_height + high) * 3; i += 3) {
                int textureIndex = texX * m_textureSize + (i - 3 * (x * m_height + mid - height)) * m_textureSize / (6 * height);
                textureIndex *= 3;
                
                m_buffer[i + 0] = (byte)(m_textures[textureId][textureIndex + 0] * (Math.min(1.0, side * 0.7)));
                m_buffer[i + 1] = (byte)(m_textures[textureId][textureIndex + 1] * (Math.min(1.0, side * 0.7)));
                m_buffer[i + 2] = (byte)(m_textures[textureId][textureIndex + 2] * (Math.min(1.0, side * 0.7)));
            }
        }
        for (int i = (x * m_height + high) * 3; i < (x * m_height + m_height) * 3; i += 3) {
            m_buffer[i + 0] = (byte)160;
            m_buffer[i + 1] = (byte)80;
            m_buffer[i + 2] = (byte)0;
        }
    }

    private int m_width;
    private int m_height;
    private byte[] m_buffer;
    private WritableImage m_image;
    private Canvas m_canvas;
    private GraphicsContext m_gc;
    private byte[][] m_textures;
    private int m_textureSize;
}
