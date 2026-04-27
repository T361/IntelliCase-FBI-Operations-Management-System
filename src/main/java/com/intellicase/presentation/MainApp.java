package com.intellicase.presentation;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point for IntelliCase UI.
 * GRASP Controller: presentation shell orchestrating the SPA startup.
 */
public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/RootLayout.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/ui/CyberpunkUI.css").toExternalForm());
        stage.setTitle("IntelliCase: FBI Operations Management System");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

