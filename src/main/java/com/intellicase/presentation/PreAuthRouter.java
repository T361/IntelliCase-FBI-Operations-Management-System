package com.intellicase.presentation;

import java.io.IOException;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Singleton SPA router for the pre-authentication screen flow.
 * Manages the PreAuthLayout content pane independently of the
 * main application ViewRouter.
 */
public final class PreAuthRouter {
    private static PreAuthRouter instance;
    private StackPane contentPane;

    private PreAuthRouter() {
    }

    public static synchronized PreAuthRouter getInstance() {
        if (instance == null) {
            instance = new PreAuthRouter();
        }
        return instance;
    }

    public void setContentPane(StackPane pane) {
        this.contentPane = pane;
    }

    /** Fade-swap to the given FXML path inside the pre-auth layout. */
    public void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();
            view.setOpacity(0);
            if (!contentPane.getChildren().isEmpty()) {
                Node old = contentPane.getChildren().get(
                    contentPane.getChildren().size() - 1);
                FadeTransition out = new FadeTransition(Duration.millis(180), old);
                out.setToValue(0);
                out.setOnFinished(e -> {
                    contentPane.getChildren().setAll(view);
                    fadeIn(view);
                });
                out.play();
            } else {
                contentPane.getChildren().setAll(view);
                fadeIn(view);
            }
        } catch (IOException ex) {
            System.err.println("[PreAuthRouter] Failed: " + fxmlPath
                + " — " + ex.getMessage());
        }
    }

    private void fadeIn(Node node) {
        FadeTransition in = new FadeTransition(Duration.millis(250), node);
        in.setFromValue(0);
        in.setToValue(1);
        in.play();
    }
}
