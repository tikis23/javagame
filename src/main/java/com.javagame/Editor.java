package com.javagame;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.canvas.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import java.util.function.UnaryOperator;
import java.nio.file.Paths;
import javafx.geometry.Point2D;

final public class Editor {
    public Editor(int windowWidth, int windowHeight) {
        m_width = windowWidth - 200;
        m_height = windowHeight - 100;
        m_exit = false;
        m_cameraX = 0;
        m_cameraY = 0;
        m_selectedImage = 0;
        m_map = new WorldMap(null);
        m_setPlayer = false;

        // setup canvas
        m_canvas = new Canvas(m_width, m_height);
        m_gc = m_canvas.getGraphicsContext2D();

        // setup buttons
        TextField newFileName = createTextField();
        Button saveButton = createButton("Save");
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                String name = newFileName.getText();
                if (name != null && !name.isEmpty()) {
                    m_map.saveToFile(name);
                }
            }
        });
        Button loadButton = createButton("Load");
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                String name = newFileName.getText();
                if (name != null && !name.isEmpty()) {
                    m_map.loadFromFile(name);
                }
            }
        });
        Button exitButton = createButton("Exit");
        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_exit = true;
            }
        });
        Button newButton = createButton("New");
        newButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                newFileName.setText("");
                m_map = new WorldMap(null);
            }
        });
        // resize input
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([1-9][0-9]*)?")) {
                return change;
            }
            return null;
        };
        TextField mapSizeX = createTextField();
        mapSizeX.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        mapSizeX.setText(String.valueOf(m_map.getMapWidth()));
        TextField mapSizeY = createTextField();
        mapSizeY.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        mapSizeY.setText(String.valueOf(m_map.getMapHeight()));

        Button resizeButton = createButton("Resize");
        resizeButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (mapSizeX.getText() == null || mapSizeY.getText() == null) return;
                if (mapSizeX.getText().isEmpty() || mapSizeY.getText().isEmpty()) return;
                try {
                    m_map.resize(Integer.parseInt(mapSizeX.getText()), Integer.parseInt(mapSizeX.getText()));
                } catch (NumberFormatException e) {
                }
            }
        });
        Button setPlayer = createButton("Set player");
        setPlayer.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_setPlayer = true;
            }
        });
 
        VBox buttons = new VBox(20);
        buttons.setPadding(new Insets(10));
        buttons.getChildren().addAll(newFileName, saveButton, loadButton, exitButton, newButton,
                mapSizeX, mapSizeY, resizeButton, setPlayer);

        // images
        HBox images = new HBox(20);
        images.setPadding(new Insets(10));
        m_images = new Image[2];
        for (int i = 0; i < m_images.length; i++) {
            m_images[i] = new Image("file:" + Paths.get("textures/" + (i + 1) + ".jpg").toString());
            Button imgButton = createButton(null);
            imgButton.setGraphic(new ImageView(m_images[i]));
            images.getChildren().add(imgButton);
            final int imgId = i;
            imgButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    m_selectedImage = imgId;
                }
            });
        }

        // layout
        HBox canvasAndButtons = new HBox(20);
        canvasAndButtons.getChildren().addAll(m_canvas, buttons);
        VBox root = new VBox();
        root.getChildren().addAll(canvasAndButtons, images);
        Background background = new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1.0), null, null));
        root.setBackground(background);

        m_scene = new Scene(root);
    }
    public void run(Input input, double dt) {
        double speed = 0.5 * dt;
        if (input.isHeld("W")) m_cameraY += speed;
        if (input.isHeld("S")) m_cameraY -= speed;
        if (input.isHeld("D")) m_cameraX -= speed;
        if (input.isHeld("A")) m_cameraX += speed;

        int tileSize = 20;
        double mapMinX = m_cameraX;
        double mapMinY = m_cameraY;
        double mapMaxX = m_cameraX + m_map.getMapWidth() * tileSize;
        double mapMaxY = m_cameraY + m_map.getMapHeight() * tileSize;

        // clear screen
        m_gc.setFill(Color.BLACK);
        m_gc.fillRect(0, 0, m_width, m_height);

        // draw tiles
        for (int j = 0; j < m_map.getMapHeight(); j++) {
            for (int i = 0; i < m_map.getMapWidth(); i++) {
                int tile = m_map.getMap()[j * m_map.getMapWidth() + i];
                if (tile > 0) {
                    m_gc.drawImage(m_images[tile - 1], mapMinX + i * tileSize, mapMinY + j * tileSize, tileSize, tileSize);
                }
            }
        }

        // draw map border
        m_gc.setStroke(Color.BLUE);
        m_gc.strokeLine(mapMinX, mapMinY, mapMaxX, mapMinY);   
        m_gc.strokeLine(mapMinX, mapMinY, mapMinX, mapMaxY);   
        m_gc.strokeLine(mapMaxX, mapMaxY, mapMaxX, mapMinY);   
        m_gc.strokeLine(mapMaxX, mapMaxY, mapMinX, mapMaxY);
        
        // draw player
        double playerR = 5;
        if (!m_setPlayerDir) {
            m_gc.setStroke(Color.CHARTREUSE);
            m_gc.strokeLine(mapMinX + m_map.getPlayerPosX() * tileSize, mapMinY + m_map.getPlayerPosY() * tileSize,
                            mapMinX + m_map.getPlayerPosX() * tileSize + m_map.getPlayerDirX() * tileSize * 0.5,
                            mapMinY + m_map.getPlayerPosY() * tileSize + m_map.getPlayerDirY() * tileSize * 0.5);
        }
        m_gc.setFill(Color.GREEN);
        m_gc.fillOval(mapMinX + m_map.getPlayerPosX() * tileSize - playerR,
                      mapMinY + m_map.getPlayerPosY() * tileSize - playerR,
                      playerR * 2, playerR * 2);

        double mouseX = input.getMousePosX();
        double mouseY = input.getMousePosY();
        // check if mouse is in canvas
        if (mouseX >= 0 && mouseY >= 0 && mouseX < m_width && mouseY < m_height) {
            int tileX = (int)(mouseX - mapMinX) / tileSize - (mouseX < mapMinX ? 1 : 0);
            int tileY = (int)(mouseY - mapMinY) / tileSize - (mouseY < mapMinY ? 1 : 0);
            if (m_setPlayer) {
                m_selectedImage = -1;
                // draw new player
                m_gc.setFill(Color.GREEN);
                m_gc.fillOval(mouseX - playerR, mouseY - playerR, playerR * 2, playerR * 2);
                if (input.isPressed("MOUSE_PRIMARY")) {
                    if (tileX >= 0 && tileY >= 0 && tileX < m_map.getMapWidth() && tileY < m_map.getMapHeight()) {
                        if (m_map.getMap()[tileY * m_map.getMapWidth() + tileX] == 0) {
                            m_map.setPlayerPos((mouseX - mapMinX) / tileSize, (mouseY - mapMinY) / tileSize);
                            m_setPlayer = false;
                            m_setPlayerDir = true;
                        }
                    }
                } else if (input.isPressed("MOUSE_SECONDARY")) {
                    m_setPlayer = false;
                }
            } else if (m_setPlayerDir) {
                m_selectedImage = -1;
                // draw new player dir
                Point2D newDir = new Point2D(mapMinX + m_map.getPlayerPosX() * tileSize, mapMinY + m_map.getPlayerPosY() * tileSize);
                newDir = new Point2D(mouseX, mouseY).subtract(newDir).normalize();
                m_gc.setStroke(Color.CHARTREUSE);
                m_gc.strokeLine(mapMinX + m_map.getPlayerPosX() * tileSize, mapMinY + m_map.getPlayerPosY() * tileSize,
                                mapMinX + m_map.getPlayerPosX() * tileSize + newDir.getX() * tileSize * 0.5,
                                mapMinY + m_map.getPlayerPosY() * tileSize + newDir.getY() * tileSize * 0.5);
                if (input.isPressed("MOUSE_PRIMARY") || input.isPressed("MOUSE_SECONDARY")) {
                    m_map.setPlayerDir(newDir.getX(), newDir.getY());
                    m_setPlayerDir = false;
                }
            } else {
                // draw selected tile as a preview
                if (m_selectedImage >= 0 && m_selectedImage < m_images.length) {
                    m_gc.drawImage(m_images[m_selectedImage], m_cameraX + tileX * tileSize,
                                m_cameraY + tileY * tileSize, tileSize, tileSize);
                }
                // check if mouse in map
                if (tileX >= 0 && tileY >= 0 && tileX < m_map.getMapWidth() && tileY < m_map.getMapHeight()) {
                    if (m_selectedImage >= 0 && m_selectedImage < m_images.length && input.isHeld("MOUSE_PRIMARY")) {
                        int playerTileX = (int)m_map.getPlayerPosX();
                        int playerTileY = (int)m_map.getPlayerPosY();
                        if (tileX != playerTileX || tileY != playerTileY) {
                            m_map.getMap()[tileY * m_map.getMapWidth() + tileX] = m_selectedImage + 1;
                        }
                    } else if (input.isHeld("MOUSE_SECONDARY")) {
                        m_map.getMap()[tileY * m_map.getMapWidth() + tileX] = 0;
                    }
                }
            }
        }
    }
    public void resize(int x, int y) {
        m_width = x - 200;
        m_height = y - 100;
        m_canvas.setWidth(m_width);
        m_canvas.setHeight(m_height);
    }
    public Scene getScene() {
        return m_scene;
    }
    public boolean shouldExit() {
        if (m_exit) {
            m_exit = false;
            return true;
        }
        return false;
    }

    private Button createButton(String text) {
        Button bt = new Button(text);
        bt.setStyle("-fx-background-color: #333433; -fx-text-fill: white; -fx-font-size: 20px;");
        bt.setOnMouseEntered(e -> bt.setStyle("-fx-background-color: #444544; -fx-text-fill: white; -fx-font-size: 20px;"));
        bt.setOnMouseExited(e ->  bt.setStyle("-fx-background-color: #333433; -fx-text-fill: white; -fx-font-size: 20px;"));
        return bt;
    }
    private TextField createTextField() {
        TextField tf = new TextField();
        tf.setStyle("-fx-background-color: #333433; -fx-text-fill: white; -fx-font-size: 20px;");
        tf.setOnMouseEntered(e -> tf.setStyle("-fx-background-color: #444544; -fx-text-fill: white; -fx-font-size: 20px;"));
        tf.setOnMouseExited(e ->  tf.setStyle("-fx-background-color: #333433; -fx-text-fill: white; -fx-font-size: 20px;"));
        return tf;
    }

    private boolean m_setPlayerDir;
    private boolean m_setPlayer;
    private int m_selectedImage;
    private WorldMap m_map;
    private Input m_input;
    private double m_cameraX;
    private double m_cameraY;
    private int m_width;
    private int m_height;
    private Image[] m_images;
    private Canvas m_canvas;
    private GraphicsContext m_gc;
    private Scene m_scene;
    private boolean m_exit;
}
