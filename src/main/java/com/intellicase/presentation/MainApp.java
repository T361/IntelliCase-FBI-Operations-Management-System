package com.intellicase.presentation;

import java.io.IOException;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX entry point for IntelliCase UI.
 * GRASP Controller: presentation shell orchestrating the SPA startup.
 */
public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        AppStageManager.setPrimaryStage(stage);
        stage.setTitle("IntelliCase: FBI Operations Management System");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setMaximized(true);
        stage.show();
        AppStageManager.showLanding();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

