package com.intellicase.presentation;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Utility class for switching the primary Stage scene.
 * Used to transition between the pre-auth landing flow
 * and the authenticated main application shell.
 */
public final class AppStageManager {
    private static Stage primaryStage;

    private AppStageManager() {
    }

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    private static void setRootOrNewScene(Parent root) {
        if (primaryStage.getScene() == null) {
            Scene scene = new Scene(root, 1280, 820);
            scene.getStylesheets().add(
                AppStageManager.class.getResource("/ui/CyberpunkUI.css").toExternalForm());
            primaryStage.setScene(scene);
        } else {
            primaryStage.getScene().setRoot(root);
        }
    }

    /** Switch to pre-auth landing page (no sidebar). */
    public static void showLanding() {
        try {
            FXMLLoader loader = new FXMLLoader(
                AppStageManager.class.getResource("/ui/PreAuthLayout.fxml"));
            setRootOrNewScene(loader.load());
            PreAuthRouter.getInstance().navigateTo("/ui/LandingPage.fxml");
        } catch (IOException ex) {
            System.err.println("[AppStageManager] Failed to show landing: " + ex.getMessage());
        }
    }

    /** Switch to the civilian public case portal (PUBLIC_USER role). */
    public static void showPublicPortal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                AppStageManager.class.getResource("/ui/PublicPortal.fxml"));
            setRootOrNewScene(loader.load());
        } catch (IOException ex) {
            System.err.println("[AppStageManager] Failed to show public portal: "
                + ex.getMessage());
        }
    }

    /** Switch to authenticated main app (with sidebar). */
    public static void showMainApp() {
        try {
            FXMLLoader loader = new FXMLLoader(
                AppStageManager.class.getResource("/ui/RootLayout.fxml"));
            setRootOrNewScene(loader.load());
        } catch (IOException ex) {
            System.err.println("[AppStageManager] Failed to show main app: " + ex.getMessage());
        }
    }
}
