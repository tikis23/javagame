package com.javagame;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.geometry.Point2D;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        m_depthBuffer = new double[m_width];

        // load textures
        String[] textureList = ImageList.get();
        if (textureList == null) {
            m_textures = new byte[1][ImageList.TEXTURE_SIZE*ImageList.TEXTURE_SIZE*3];
        } else {
            m_textures = new byte[textureList.length + 1][ImageList.TEXTURE_SIZE*ImageList.TEXTURE_SIZE*3];
            byte[] temp_buff = new byte[ImageList.TEXTURE_SIZE*ImageList.TEXTURE_SIZE*4];
            for (int t = 0; t < textureList.length; t++) {
                Image img = new Image("file:" + textureList[t]);
                img.getPixelReader().getPixels(0, 0, ImageList.TEXTURE_SIZE, ImageList.TEXTURE_SIZE,
                            WritablePixelFormat.getByteBgraInstance(), temp_buff, 0, ImageList.TEXTURE_SIZE*4);
                // copy rgb only and rotate
                for (int i = 0; i < ImageList.TEXTURE_SIZE; i++) {
                    for (int j = 0; j < ImageList.TEXTURE_SIZE; j++) {
                        m_textures[t + 1][(i * ImageList.TEXTURE_SIZE + (ImageList.TEXTURE_SIZE - j - 1)) * 3 + 0] = temp_buff[(j * ImageList.TEXTURE_SIZE + i) * 4 + 2];
                        m_textures[t + 1][(i * ImageList.TEXTURE_SIZE + (ImageList.TEXTURE_SIZE - j - 1)) * 3 + 1] = temp_buff[(j * ImageList.TEXTURE_SIZE + i) * 4 + 1];
                        m_textures[t + 1][(i * ImageList.TEXTURE_SIZE + (ImageList.TEXTURE_SIZE - j - 1)) * 3 + 2] = temp_buff[(j * ImageList.TEXTURE_SIZE + i) * 4 + 0];
                    }
                }
            }
        }
        // default texture
        for (int i = 0; i < ImageList.TEXTURE_SIZE * ImageList.TEXTURE_SIZE * 3; i += 3) {
            m_textures[0][i + 0] = (byte)255;
            m_textures[0][i + 1] = (byte)0;
            m_textures[0][i + 2] = (byte)255;
        }
    }
    public void resize(int x, int y) {
        m_width = x;
        m_height = y;
        m_canvas.setWidth(x);
        m_canvas.setHeight(y);
        m_image = new WritableImage(m_height, m_width);
        m_buffer = new byte[m_width*m_height*3];
        m_depthBuffer = new double[m_width];
    }
    public void render(World world) {
        PixelWriter writer = m_image.getPixelWriter();
        
        int[] map = world.getMap();
        // get perpendicular vector to dir, used to get rayDir
        Point2D perpDir = new Point2D(-world.player.getDir().getY(), world.player.getDir().getX());
        for (int i = 0; i < m_width; i++) {
            // clear depth
            m_depthBuffer[i] = 0;
            // convert [0, width) to [-1, 1]
            double dirOffset = i / (double)m_width * 2.0 - 1.0;
            dirOffset *= 0.6; // decrease FOV
            dirOffset *= (double)m_width / m_height; // correct aspect ratio
            Point2D rayOrigin = world.player.getPos();
            Point2D rayDir = world.player.getDir().add(perpDir.multiply(dirOffset)); // dont normalize to remove fish-eye

            boolean hit = false;
            int material = 0;
            ArrayList<AnimatedLine> animated = new ArrayList<>();
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
                int tileIndex = gridY * world.getMapWidth() + gridX;
                int tile = map[tileIndex];
                if (tile > 0) {
                    material = tile;
                    hit = true;
                    break;
                }
                if (tile < 0) {
                    animated.add(new AnimatedLine(-tile, tileIndex, hitDir, new Point2D(sideDist.getX(), sideDist.getY())));
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
            {
                double hitDist = hitDir == 1 ? sideDist.getX() - deltaDist.getX() : sideDist.getY() - deltaDist.getY();
                if (hitDist <= 0) hitDist = 1;
                double texX = 0;
                if (hitDir == 1) {
                    texX = rayOrigin.add(rayDir.multiply(hitDist)).getY();
                } else {
                    texX = rayOrigin.add(rayDir.multiply(hitDist)).getX();
                }
                texX -= Math.floor(texX);
                texX *= ImageList.TEXTURE_SIZE;
                if (hit) {
                    fillLine(i, (int)texX, 1.0 / hitDist, hitDir, material);
                } else {
                    fillLine(i, (int)texX, 1.0 / hitDist, hitDir, -1);
                }
                m_depthBuffer[i] = hitDist;
            }
            for (int a = animated.size() - 1; a >= 0; a--) {
                double hitDist = animated.get(a).hitDir == 1 ? animated.get(a).sideDist.getX() - deltaDist.getX() :
                                                               animated.get(a).sideDist.getY() - deltaDist.getY();
                if (hitDist <= 0) hitDist = 1;
                double texX = 0;
                if (animated.get(a).hitDir == 1) {
                    texX = rayOrigin.add(rayDir.multiply(hitDist)).getY();
                } else {
                    texX = rayOrigin.add(rayDir.multiply(hitDist)).getX();
                }
                texX -= Math.floor(texX);
                texX *= ImageList.TEXTURE_SIZE;
                Double animH = world.getAnimTileHeight(animated.get(a).tileIndex);
                if (animH == null) animH = -1.0;
                fillAnimatedLine(i, (int)texX, 1.0 / hitDist, animH, animated.get(a).hitDir, animated.get(a).material);
                m_depthBuffer[i] = hitDist;
            }
        }

        // sort entities by dist from player
        ArrayList<Entity> entities = world.getEntities();
        Collections.sort(entities, new Comparator<Entity>() {
            @Override public int compare(Entity e1, Entity e2) {
                double dist1 = world.player.getPos().distance(e1.getRigidBody().getPosition());
                double dist2 = world.player.getPos().distance(e2.getRigidBody().getPosition());
                return Double.compare(dist2, dist1);
            }
        });

        // render entities
        Point2D camDir = world.player.getDir();
        double inv = 1.0 / (perpDir.getX() * camDir.getY() - perpDir.getY() * camDir.getX());
        for (Entity ent : entities) {
            Sprite sprite = ent.getSprite();
            if (sprite == null) continue;

            double dist = world.player.getPos().distance(ent.getRigidBody().getPosition());
            if (dist == 0) continue;

            Point2D relativePos = ent.getRigidBody().getPosition().subtract(world.player.getPos());
            // project to cam space
            double projX = inv * (camDir.getY() * relativePos.getX() - camDir.getX() * relativePos.getY());
            double projY = inv * (perpDir.getX() * relativePos.getY() - perpDir.getY() * relativePos.getX());

            fillSprite(sprite, ent.getAnimTileId(), ent.getRigidBody().getRadius(), ent.getHeight(), ent.getVOffset(), projX, projY);
        }
        
        writer.setPixels(0, 0, m_height, m_width, PixelFormat.getByteRgbInstance(), m_buffer, 0, m_height*3);
        m_gc.drawImage(m_image, -m_height, 0);
        
        // draw gun sprite on top
        Sprite gun = world.player.getGunSprite();
        if (gun != null) {
            int gunFrame = world.player.getGunSpriteFrame();
            int size = (int)(m_height / 2.5);
            m_gc.drawImage(gun.getImageTile(gunFrame), -m_height, m_width / 2 - size / 2, size, size);
        }

        // make screen red if health is lower
        m_gc.setFill(Color.rgb(200, 10, 10, 0.8 * (1.0 - world.player.getHealth() / (double)world.player.getMaxHealth())));
        m_gc.fillRect(-m_height, 0, m_height, m_width);
    }
    private void fillLine(int x, int texX, double h, int side, int textureId) {
        if (textureId >= m_textures.length) textureId = 0;
        int height = (int)(m_height * h * 0.5);
        int mid = m_height / 2;
        int low = Math.max(0, mid - height);
        int high = Math.min(m_height, mid + height);
        
        for (int i = x * m_height * 3; i < (x * m_height + low) * 3; i += 3) {
            m_buffer[i + 0] = (byte)80;
            m_buffer[i + 1] = (byte)40;
            m_buffer[i + 2] = (byte)0;
        }

        if (textureId < 0) {
            for (int i = (x * m_height + low) * 3; i < (x * m_height + high) * 3; i += 3) {
                m_buffer[i + 0] = (byte)(60 * (Math.min(1.0, side * 0.7)));
                m_buffer[i + 1] = (byte)(60 * (Math.min(1.0, side * 0.7)));
                m_buffer[i + 2] = (byte)(60 * (Math.min(1.0, side * 0.7)));
            }
        } else {
            for (int i = (x * m_height + low) * 3; i < (x * m_height + high) * 3; i += 3) {
                int textureIndex = texX * ImageList.TEXTURE_SIZE + (i - 3 * (x * m_height + mid - height)) * ImageList.TEXTURE_SIZE / (6 * height);
                textureIndex *= 3;

                m_buffer[i + 0] = (byte)(((int)m_textures[textureId][textureIndex + 0] & 0xFF) * (Math.min(1.0, side * 0.7)));
                m_buffer[i + 1] = (byte)(((int)m_textures[textureId][textureIndex + 1] & 0xFF) * (Math.min(1.0, side * 0.7)));
                m_buffer[i + 2] = (byte)(((int)m_textures[textureId][textureIndex + 2] & 0xFF) * (Math.min(1.0, side * 0.7)));
            }
        }
        for (int i = (x * m_height + high) * 3; i < (x * m_height + m_height) * 3; i += 3) {
            m_buffer[i + 0] = (byte)160;
            m_buffer[i + 1] = (byte)80;
            m_buffer[i + 2] = (byte)0;
        }
    }
    private void fillAnimatedLine(int x, int texX, double h, double animH, int side, int textureId) {
        if (textureId >= m_textures.length) textureId = 0;
        int height = (int)(m_height * h * 0.5);
        int mid = m_height / 2;
        int low = Math.max(0, mid - height);
        int high = (int)Math.min(m_height, mid + height * animH);
        
        for (int i = (x * m_height + low) * 3; i < (x * m_height + high) * 3; i += 3) {
            int textureIndex = texX * ImageList.TEXTURE_SIZE + (i - 3 * (x * m_height + mid - height)) * ImageList.TEXTURE_SIZE / (6 * height);
            textureIndex *= 3;
            m_buffer[i + 0] = (byte)(((int)m_textures[textureId][textureIndex + 0] & 0xFF) * (Math.min(1.0, side * 0.7)));
            m_buffer[i + 1] = (byte)(((int)m_textures[textureId][textureIndex + 1] & 0xFF) * (Math.min(1.0, side * 0.7)));
            m_buffer[i + 2] = (byte)(((int)m_textures[textureId][textureIndex + 2] & 0xFF) * (Math.min(1.0, side * 0.7)));
        }
    }
    private void fillSprite(Sprite sprite, int tileId, double entityWidth, double entityHeight, double vOffset, double projX, double projY) {
        byte[] img = sprite.getTile(tileId);
        if (img == null) return;

        double ndc = projX / projY;
        double centerX = (ndc + 1.0) / 2.0 * m_width;
        double spriteWidth = entityWidth / projY * m_height;
        double spriteHeight = entityHeight / projY * m_height;
        double verticalOffset = (vOffset - 0.5) / projY * m_height;

        int minX = (int)(centerX - spriteWidth);
        int maxX = (int)(centerX + spriteWidth);
        int minY = (int)(m_height / 2 + verticalOffset);
        int maxY = (int)(m_height / 2 + verticalOffset + spriteHeight);
            
        int iterMinX = Math.min(m_width - 1, Math.max(0, minX));
        int iterMaxX = Math.min(m_width - 1, Math.max(0, maxX));
        int iterMinY = Math.min(m_height - 1, Math.max(0, minY));
        int iterMaxY = Math.min(m_height - 1, Math.max(0, maxY));
        
        for (int x = iterMinX; x < iterMaxX; x++) {
            if (m_depthBuffer[x] < projY) continue;
            int texIndX = (x - minX) * sprite.getTileSize() / (maxX - minX) * sprite.getTileSize();
            for (int i = x * m_height + iterMinY; i < x * m_height + iterMaxY; i++) {
                int textureIndex = texIndX + (i - x * m_height - minY) * sprite.getTileSize() / (maxY - minY);
                textureIndex *= 4;
                double a = (double)((int)img[textureIndex + 3] & 0xFF) / 255.0;
                m_buffer[i * 3 + 0] = blendAlpha(m_buffer[i * 3 + 0], img[textureIndex + 0], a);
                m_buffer[i * 3 + 1] = blendAlpha(m_buffer[i * 3 + 1], img[textureIndex + 1], a);
                m_buffer[i * 3 + 2] = blendAlpha(m_buffer[i * 3 + 2], img[textureIndex + 2], a);
            }
        }
    }
    private byte blendAlpha(byte cOld, byte cNew, double alpha) {
        return (byte)(alpha * ((int)cNew & 0xFF) + (1.0 - alpha) * ((int)cOld & 0xFF));
    }

    private class AnimatedLine {
        public AnimatedLine(int Material, int TileIndex, int HitDir, Point2D SideDist) {
            material = Material;
            tileIndex = TileIndex;
            hitDir = HitDir;
            sideDist = SideDist;
        }
        public int material;
        public int tileIndex;
        public int hitDir;
        public Point2D sideDist;
    }

    private int m_width;
    private int m_height;
    private byte[] m_buffer;
    private double[] m_depthBuffer;
    private WritableImage m_image;
    private Canvas m_canvas;
    private GraphicsContext m_gc;
    private byte[][] m_textures;
}
