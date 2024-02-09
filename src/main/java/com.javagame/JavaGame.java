package com.javagame;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

final public class JavaGame extends Application {
    @Override public void start(Stage stage) {
        stage.setTitle("Java game!");

        GameLoop gameLoop = new GameLoop();
        stage.setScene(gameLoop.getScene());
        gameLoop.start();

        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}