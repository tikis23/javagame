package com.javagame;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

final public class GameLoop {
    public GameLoop(Stage mainStage, int windowWidth, int windowHeight) {
        m_root = new Group();
        m_scene = new Scene(m_root, windowWidth, windowHeight);
        m_oldSizeX = windowWidth;
        m_oldSizeY = windowHeight;
        m_pause = true;
        m_mainStage = mainStage;
        m_edit = false;

        // Menu setup
        m_menu = new Menu();
        
        m_stackPane = new StackPane();
        m_stackPane.getChildren().addAll(m_scene.getRoot(), m_menu.getScene().getRoot());
        m_stackPaneScene = new Scene(m_stackPane, windowWidth, windowHeight);
    }
    public Scene getScene() {
        return m_stackPaneScene;
    }
    public void start() {
        Input input = new Input(m_stackPaneScene);
        Renderer renderer = new Renderer(m_root, m_oldSizeX, m_oldSizeY);
        World world = new World();
        renderer.render(world); // just to have something in the background

        m_menu.setOnPlay(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_pause = false;
                m_stackPane.getChildren().remove(m_menu.getScene().getRoot());
            }
        });
        m_menu.setOnEdit(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_edit = true;
            }
        });
        m_menu.setOnExit(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_mainStage.close();
            }
        });

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
                if ((int)m_stackPaneScene.getWidth() != m_oldSizeX || (int)m_stackPaneScene.getHeight() != m_oldSizeY) {
                    m_oldSizeX = (int)m_stackPaneScene.getWidth();
                    m_oldSizeY = (int)m_stackPaneScene.getHeight();
                    renderer.resize(m_oldSizeX, m_oldSizeY);
                    if (m_pause) renderer.render(world);
                }
                
                input.poll();
                if (!m_pause && input.isPressed("ESCAPE")) {
                    m_pause = true;
                    m_stackPane.getChildren().add(m_menu.getScene().getRoot());
                }

                if (m_pause) {
                    // Menu menu = new Menu();
                } else {
                    world.update(input, dt);
                    renderer.render(world);
                }
            }
        }.start();
    }

    private boolean m_edit;
    private Stage m_mainStage;
    private Menu m_menu;
    private Scene m_stackPaneScene;
    private StackPane m_stackPane;
    private boolean m_pause;
    private int m_frames;
    private long m_frameTime;
    private long m_prevNanoTime;
    private int m_oldSizeX;
    private int m_oldSizeY;
    private Scene m_scene;
    private Group m_root;
}
