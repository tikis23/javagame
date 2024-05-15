package com.javagame;

import javafx.application.Application;
import javafx.stage.Stage;

final public class JavaGame extends Application {
    @Override public void start(Stage stage) {
        stage.setTitle("Java game!");

        GameLoop gameLoop = new GameLoop(stage, 1280, 720);
        stage.setScene(gameLoop.getScene());
        stage.show();
        
        gameLoop.start();
    }
    public static void runGame() {
        launch();
    }
}