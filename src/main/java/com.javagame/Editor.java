package com.javagame;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
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
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;
import java.util.ArrayList;
import java.util.Arrays;

final public class Editor {
    public Editor(int windowWidth, int windowHeight) {
        m_width = windowWidth - 200;
        m_height = windowHeight - 100;
        m_exit = false;
        m_cameraX = 0;
        m_cameraY = 0;
        m_selectedImage = -1;
        m_map = new WorldMap(null);
        m_setPlayer = false;
        m_addExits = false;
        m_removeExits = false;
        m_enemySelected = null;
        Background background = new Background(new BackgroundFill(Color.rgb(20, 20, 20, 1.0), null, null));

        // setup canvas
        m_canvas = new Canvas(m_width, m_height);
        m_gc = m_canvas.getGraphicsContext2D();

        // setup buttons
        TextField newFileName = createTextField();
        newFileName.setPromptText("Map name");
        Button saveButton = createButton("Save");
        Button loadButton = createButton("Load");
        Button exitButton = createButton("Exit");
        exitButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_exit = true;
                m_setPlayer = false;
                m_addExits = false;
                m_removeExits = false;
                m_enemySelected = null;
            }
        });
        Button newButton = createButton("New");

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
        mapSizeX.setPromptText("Map width");
        TextField mapSizeY = createTextField();
        mapSizeY.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
        mapSizeY.setText(String.valueOf(m_map.getMapHeight()));
        mapSizeY.setPromptText("Map height");

        Button resizeButton = createButton("Resize");
        resizeButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                if (mapSizeX.getText() == null || mapSizeY.getText() == null) return;
                if (mapSizeX.getText().isEmpty() || mapSizeY.getText().isEmpty()) return;
                try {
                    m_map.resize(Integer.parseInt(mapSizeX.getText()), Integer.parseInt(mapSizeY.getText()));
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
        TextField mapTarget = createTextField();
        mapTarget.setPromptText("Target map");

        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                String target = mapTarget.getText();
                if (target != null) {
                    m_map.setTargetMap(target);
                }
                String name = newFileName.getText();
                if (name != null && !name.isEmpty()) {
                    m_map.saveToFile(name);
                }
            }
        });
        loadButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                String name = newFileName.getText();
                if (name != null && !name.isEmpty()) {
                    m_map.loadFromFile(name);
                    mapSizeX.setText(String.valueOf(m_map.getMapWidth()));
                    mapSizeY.setText(String.valueOf(m_map.getMapHeight()));
                    mapTarget.setText(m_map.getTargetMap());
                }
            }
        });
        newButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                newFileName.setText("");
                m_map = new WorldMap(null);
                mapSizeX.setText(String.valueOf(m_map.getMapWidth()));
                mapSizeY.setText(String.valueOf(m_map.getMapHeight()));
                mapTarget.setText(m_map.getTargetMap());
            }
        });

        Button addExits = createButton("Add exits");
        addExits.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_removeExits = false;
                m_addExits = true;
            }
        });
        Button removeExits = createButton("Remove exits");
        removeExits.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_addExits = false;
                m_removeExits = true;
            }
        });

        // enemy spawners
        ArrayList<Button> enemyButtons = new ArrayList<>();
        for (String enemyName : WorldMap.enemyNames.keySet()) {
            Button enemyButton = createButton("Add " + enemyName);
            enemyButtons.add(enemyButton);
            enemyButton.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent event) {
                    m_enemySelected = enemyName;
                }
            });
        }
        Button deleteEnemy = createButton("Remove enemy");
        deleteEnemy.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                m_enemySelected = "__ENEMY__DELETE__";
            }
        });

        VBox buttonBox = new VBox(20);
        buttonBox.setPadding(new Insets(0, 5, 0, 0));
        buttonBox.getChildren().addAll(newFileName, saveButton, loadButton, exitButton, newButton,
                mapSizeX, mapSizeY, resizeButton, setPlayer, mapTarget, addExits, removeExits, deleteEnemy);
        buttonBox.getChildren().addAll(enemyButtons);
        m_buttons = new ScrollPane();
        String cssButtons = ".scroll-bar:vertical .thumb {-fx-background-color: rgb(40, 40, 40);}" + 
            ".scroll-bar:vertical .track {-fx-background-color: rgb(10, 10, 10);}" +
            ".scroll-bar:vertical .increment-button, .decrement-button {-fx-background-color :transparent;"+
            "-fx-background-radius : 0.0; -fx-padding :0.0 5.0 0.0 0.0;}" +
            ".scroll-bar:vertical .increment-arrow, .decrement-arrow {-fx-shape : \" \"; -fx-padding :0.0 0.15em;}";
        m_buttons.getStylesheets().add("data:text/css," + cssButtons);
        m_buttons.setHmax(0.0);
        m_buttons.setBackground(background);
        m_buttons.setMaxHeight(m_height);
        m_buttons.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        m_buttons.setContent(buttonBox);
        m_buttons.setBackground(background);     
        m_buttons.setPadding(new Insets(10));
        buttonBox.setBackground(background);     

        // images
        ScrollPane imagePane = new ScrollPane();
        String cssImages = ".scroll-bar:horizontal .thumb {-fx-background-color: rgb(40, 40, 40);}" + 
            ".scroll-bar:horizontal .track {-fx-background-color: rgb(10, 10, 10);}" +
            ".scroll-bar:horizontal .increment-button, .decrement-button {-fx-background-color :transparent;"+
            "-fx-background-radius : 0.0; -fx-padding :0.0 0.0 5.0 0.0;}" +
            ".scroll-bar:horizontal .increment-arrow, .decrement-arrow {-fx-shape : \" \"; -fx-padding :0.15em 0.0;}";
                            
        imagePane.getStylesheets().add("data:text/css," + cssImages);
        imagePane.setVmax(0.0);
        imagePane.setBackground(background);
        imagePane.setFitToWidth(true);
        imagePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        HBox images = new HBox(20);
        imagePane.setContent(images);
        images.setBackground(background);
        images.setPadding(new Insets(10));
        String[] textureList = ImageList.get();
        if (textureList == null) { // empty texture
            m_images = new Image[1];
        } else {
            m_images = new Image[textureList.length + 1];
            for (int i = 0; i < textureList.length; i++) {
                m_images[i + 1] = new Image("file:" + textureList[i]);
                Button imgButton = createButton(null);
                imgButton.setGraphic(new ImageView(m_images[i + 1]));
                images.getChildren().add(imgButton);
                final int imgId = i + 1;
                imgButton.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent event) {
                        m_selectedImage = imgId;
                        m_addExits = false;
                        m_removeExits = false;
                    }
                });
            }
        }
        // create default texture
        {
            byte[] pixels = new byte[ImageList.TEXTURE_SIZE * ImageList.TEXTURE_SIZE * 3];
            for (int i = 0; i < ImageList.TEXTURE_SIZE * ImageList.TEXTURE_SIZE * 3; i += 3) {
                pixels[i + 0] = (byte)255;
                pixels[i + 1] = (byte)0;
                pixels[i + 2] = (byte)255;
            }

            WritableImage img = new WritableImage(ImageList.TEXTURE_SIZE, ImageList.TEXTURE_SIZE);
            PixelWriter pw = img.getPixelWriter();
            pw.setPixels(0, 0, ImageList.TEXTURE_SIZE, ImageList.TEXTURE_SIZE, PixelFormat.getByteRgbInstance(),
                         pixels, 0, ImageList.TEXTURE_SIZE * 3);
            m_images[0] = img;
        }
        imagePane.addEventFilter(ScrollEvent.SCROLL, event -> {
            double dist = event.getDeltaY() / m_images.length / 100;
            imagePane.setHvalue(imagePane.getHvalue() - dist);
            event.consume();
        });

        // layout
        HBox canvasAndButtons = new HBox(20);
        canvasAndButtons.getChildren().addAll(m_canvas, m_buttons);
        VBox root = new VBox();
        root.getChildren().addAll(canvasAndButtons, imagePane);
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
                    if (tile >= m_images.length) tile = 0;
                    m_gc.drawImage(m_images[tile], mapMinX + i * tileSize, mapMinY + j * tileSize, tileSize, tileSize);
                }
            }
        }

        // draw map border
        m_gc.setStroke(Color.BLUE);
        m_gc.strokeLine(mapMinX, mapMinY, mapMaxX, mapMinY);   
        m_gc.strokeLine(mapMinX, mapMinY, mapMinX, mapMaxY);   
        m_gc.strokeLine(mapMaxX, mapMaxY, mapMaxX, mapMinY);   
        m_gc.strokeLine(mapMaxX, mapMaxY, mapMinX, mapMaxY);
        
        // draw exits
        m_gc.setStroke(Color.RED);
        for (int[] exitPos : m_map.getExits()) {
            double minX = mapMinX + exitPos[0] * tileSize;
            double minY = mapMinY + exitPos[1] * tileSize;
            double maxX = minX + tileSize;
            double maxY = minY + tileSize;
            m_gc.strokeLine(minX, minY, maxX, maxY);
            m_gc.strokeLine(minX, maxY, maxX, minY);
        }
        
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

        // draw enemies
        for (WorldMap.EnemyData enemy : m_map.getEnemies()) {
            // get random color from name
            int hash = enemy.name.hashCode();
            m_gc.setFill(Color.hsb(hash, 1.0, 1.0));
            m_gc.fillOval(mapMinX + enemy.pos.getX() * tileSize - playerR, mapMinY + enemy.pos.getY() * tileSize - playerR,
                          playerR * 2, playerR * 2);
        }

        double mouseX = input.getMousePosX();
        double mouseY = input.getMousePosY();

        if (m_addExits || m_removeExits) {
            if (input.isPressed("MOUSE_SECONDARY")) {
                m_addExits = false;
                m_removeExits = false;
            }
            m_selectedImage = -1;
        } 

        // check if mouse is in canvas
        if (mouseX >= 0 && mouseY >= 0 && mouseX < m_width && mouseY < m_height) {
            int tileX = (int)(mouseX - mapMinX) / tileSize - (mouseX < mapMinX ? 1 : 0);
            int tileY = (int)(mouseY - mapMinY) / tileSize - (mouseY < mapMinY ? 1 : 0);
            if (m_setPlayer) {
                m_addExits = false;
                m_removeExits = false;
                m_enemySelected = null;
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
                m_addExits = false;
                m_removeExits = false;
                m_enemySelected = null;
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
            } else if (m_enemySelected != null) {
                m_setPlayer = false;
                m_setPlayerDir = false;
                m_addExits = false;
                m_removeExits = false;
                m_selectedImage = -1;
                
                if ("__ENEMY__DELETE__".equals(m_enemySelected)) {
                    m_gc.setFill(Color.RED);
                    m_gc.fillRect(mouseX - playerR, mouseY - playerR, playerR * 2, playerR * 2);
                } else {
                    // get random color from name
                    int hash = m_enemySelected.hashCode();
                    m_gc.setFill(Color.hsb(hash, 1.0, 1.0));
                    m_gc.fillOval(mouseX - playerR, mouseY - playerR, playerR * 2, playerR * 2);
                }
                if (tileX >= 0 && tileY >= 0 && tileX < m_map.getMapWidth() && tileY < m_map.getMapHeight()) {
                    if (m_map.getMap()[tileY * m_map.getMapWidth() + tileX] == 0) {
                        Point2D pos = new Point2D((mouseX - mapMinX) / tileSize, (mouseY - mapMinY) / tileSize);
                        if ("__ENEMY__DELETE__".equals(m_enemySelected)) {
                            if (input.isHeld("MOUSE_PRIMARY")) {
                                // find closest enemies and delete
                                ArrayList<WorldMap.EnemyData> enemies = new ArrayList<>(Arrays.asList(m_map.getEnemies()));
                                double delRadius = 0.5;
                                for (int i = 0; i < enemies.size(); i++) {
                                    if (enemies.get(i).pos.distance(pos) < delRadius) {
                                        enemies.remove(i);
                                        i--;
                                    }
                                }
                                m_map.setEnemies(enemies.toArray(new WorldMap.EnemyData[0]));
                            }
                        } else if (input.isPressed("MOUSE_PRIMARY")) {
                            // create enemy at location
                            ArrayList<WorldMap.EnemyData> enemies = new ArrayList<>(Arrays.asList(m_map.getEnemies()));
                            enemies.add(new WorldMap.EnemyData(m_enemySelected, pos));
                            m_map.setEnemies(enemies.toArray(new WorldMap.EnemyData[0]));
                        }
                    }
                } 
                if (input.isPressed("MOUSE_SECONDARY")) {
                    m_enemySelected = null;
                }
            } else {
                // draw selected tile as a preview
                if (m_selectedImage >= 0) {
                    int previewImg = m_selectedImage;
                    if (previewImg >= m_images.length) previewImg = 0;
                    m_gc.drawImage(m_images[previewImg], m_cameraX + tileX * tileSize,
                                m_cameraY + tileY * tileSize, tileSize, tileSize);
                }
                // check if mouse in map
                if (tileX >= 0 && tileY >= 0 && tileX < m_map.getMapWidth() && tileY < m_map.getMapHeight()) {
                    // add/remove exits
                    if (m_addExits && input.isHeld("MOUSE_PRIMARY")) {
                        m_setPlayer = false;
                        m_setPlayerDir = false;
                        m_enemySelected = null;
                        m_selectedImage = -1;
                        int[][] exits = m_map.getExits();
                        boolean foundNone = true;
                        for (int i = 0; i < exits.length; i++) {
                            if (exits[i][0] == tileX && exits[i][1] == tileY) {
                                foundNone = false;
                                break;
                            }
                        }
                        if (foundNone) {
                            int[][] newExits = new int[exits.length + 1][2];
                            int i;
                            for (i = 0; i < exits.length; i++) {
                                newExits[i] = exits[i];
                            }
                            newExits[i][0] = tileX;
                            newExits[i][1] = tileY;
                            m_map.setExits(newExits);
                        }
                    } else if (m_removeExits && input.isHeld("MOUSE_PRIMARY")) {
                        m_setPlayer = false;
                        m_setPlayerDir = false;
                        m_enemySelected = null;
                        m_selectedImage = -1;
                        int[][] exits = m_map.getExits();
                        boolean foundNone = true;
                        for (int i = 0; i < exits.length; i++) {
                            if (exits[i][0] == tileX && exits[i][1] == tileY) {
                                foundNone = false;
                                break;
                            }
                        }
                        if (!foundNone) {
                            int[][] newExits = new int[exits.length - 1][2];
                            for (int i = 0; i < exits.length - 1; i++) {
                                if (exits[i][0] == tileX && exits[i][1] == tileY) {
                                    exits[i] = exits[exits.length - 1];
                                }
                                newExits[i] = exits[i];
                            }
                            m_map.setExits(newExits);
                        }
                    } else {   
                        if (m_selectedImage >= 0 && m_selectedImage < m_images.length && input.isHeld("MOUSE_PRIMARY")) {
                            int playerTileX = (int)m_map.getPlayerPosX();
                            int playerTileY = (int)m_map.getPlayerPosY();
                            if (tileX != playerTileX || tileY != playerTileY) {
                                m_map.getMap()[tileY * m_map.getMapWidth() + tileX] = m_selectedImage;
                            }
                        } else if (input.isHeld("MOUSE_SECONDARY")) {
                            m_map.getMap()[tileY * m_map.getMapWidth() + tileX] = 0;
                        }
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
        m_buttons.setMaxHeight(m_height);
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

    private String m_enemySelected;
    private boolean m_addExits;
    private boolean m_removeExits;
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
    private ScrollPane m_buttons;
    private GraphicsContext m_gc;
    private Scene m_scene;
    private boolean m_exit;
}
