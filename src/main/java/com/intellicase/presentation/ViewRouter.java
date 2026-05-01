package com.intellicase.presentation;

import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * GoF Singleton implementing SPA-style view routing for the JavaFX UI.
 * Manages a root StackPane and dynamically swaps FXML views with fade transitions.
 */
public final class ViewRouter {
    private static ViewRouter instance;
    private StackPane contentPane;
    private String currentView;

    private ViewRouter() {
    }

    public static synchronized ViewRouter getInstance() {
        if (instance == null) {
            instance = new ViewRouter();
        }
        return instance;
    }

    public void setContentPane(StackPane contentPane) {
        this.contentPane = contentPane;
    }

    private Runnable onNavigation;

    public void setOnNavigation(Runnable onNavigation) {
        this.onNavigation = onNavigation;
    }

    public void navigateTo(String fxmlPath) {
        if (fxmlPath.equals(currentView)) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            view.setOpacity(0);

            if (!contentPane.getChildren().isEmpty()) {
                Node oldView = contentPane.getChildren().get(contentPane.getChildren().size() - 1);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), oldView);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    contentPane.getChildren().clear();
                    contentPane.getChildren().add(view);
                    fadeIn(view);
                });
                fadeOut.play();
            } else {
                contentPane.getChildren().add(view);
                fadeIn(view);
            }
            currentView = fxmlPath;
            if (onNavigation != null) {
                onNavigation.run();
            }
        } catch (IOException ex) {
            System.err.println("[ViewRouter] Failed to load view: " + fxmlPath + " — " + ex.getMessage());
        }
    }

    private void fadeIn(Node node) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public String getCurrentView() {
        return currentView;
    }
}
