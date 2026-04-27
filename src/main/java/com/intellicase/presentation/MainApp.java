package com.intellicase.presentation;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX entry point for IntelliCase UI.
 * GRASP Controller: presentation shell orchestrating the UI startup.
 */
public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainDashboard.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 720);
        stage.setTitle("IntelliCase: FBI Operations Management System");
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(680);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
