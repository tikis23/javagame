package com.javagame;

import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.animation.AnimationTimer;

final public class GameLoop {
    public GameLoop() {
        m_root = new Group();
        m_scene = new Scene(m_root);
    }
    public Scene getScene() {
        return m_scene;
    }
    public void start() {
        Input input = new Input(m_scene);
        Renderer renderer = new Renderer(m_root, 1280, 720);
        World world = new World();

        m_prevNanoTime = System.nanoTime();
        m_frames = 0;
        m_frameTime = 0;
        new AnimationTimer() {
            public void handle(long currentNanoTime) {
                m_frames++;
                m_frameTime += currentNanoTime - m_prevNanoTime;
                if (m_frameTime > 500000000L) {
                    m_frameTime /= m_frames;
                    System.out.println("frame time: " + (double)m_frameTime / 1000000 + "ms");
                    m_frameTime = 0;
                    m_frames = 0;
                }
                double dt = (currentNanoTime - m_prevNanoTime) / 1000000.0;
                m_prevNanoTime = currentNanoTime;
                // window resize
                if ((int)m_scene.getWidth() != m_oldSizeX || (int)m_scene.getHeight() != m_oldSizeY) {
                    m_oldSizeX = (int)m_scene.getWidth();
                    m_oldSizeY = (int)m_scene.getHeight();
                    renderer.resize(m_oldSizeX, m_oldSizeY);
                }
                
                input.poll();
                world.update(input, dt);

                renderer.render(world);
            }
        }.start();
    }

    private int m_frames;
    private long m_frameTime;
    private long m_prevNanoTime;
    private int m_oldSizeX;
    private int m_oldSizeY;
    private Scene m_scene;
    private Group m_root;
}
