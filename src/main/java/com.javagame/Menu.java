package com.javagame;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

final public class Menu {
    public Menu() {
        m_playButton = createButton("Play");
        m_editButton = createButton("Edit");
        m_exitButton = createButton("Exit");

        VBox vBox = new VBox(20);
        vBox.getChildren().addAll(m_playButton, m_editButton, m_exitButton);
        
        vBox.setAlignment(Pos.CENTER);
        Background background = new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.8), null, null));
        vBox.setBackground(background);

        m_scene = new Scene(vBox);
    }
    public Scene getScene() {
        return m_scene;
    }
    public void setOnPlay(EventHandler<ActionEvent> event) {
        m_playButton.setOnAction(event);
    }
    public void setOnEdit(EventHandler<ActionEvent> event) {
        m_editButton.setOnAction(event);
    }
    public void setOnExit(EventHandler<ActionEvent> event) {
        m_exitButton.setOnAction(event);
    }

    private Button createButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #333433; -fx-text-fill: white; -fx-font-size: 20px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #444544; -fx-text-fill: white; -fx-font-size: 20px;"));
        button.setOnMouseExited(e ->  button.setStyle("-fx-background-color: #333433; -fx-text-fill: white; -fx-font-size: 20px;"));
        return button;
    }

    private Button m_playButton;
    private Button m_editButton;
    private Button m_exitButton;
    private Scene m_scene;
}
