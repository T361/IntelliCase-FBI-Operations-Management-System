package com.intellicase.presentation;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;

/**
 * Controller for PreAuthLayout.fxml.
 * Registers the content pane with PreAuthRouter and adds a
 * live particle canvas background behind the page content.
 */
public class PreAuthLayoutController {
    @FXML
    private StackPane preAuthRoot;

    @FXML
    private StackPane preAuthContent;

    @FXML
    private void initialize() {
        PreAuthRouter.getInstance().setContentPane(preAuthContent);
        addParticleBackground();
    }

    private void addParticleBackground() {
        ParticleCanvas particles = new ParticleCanvas();
        particles.widthProperty().bind(preAuthRoot.widthProperty());
        particles.heightProperty().bind(preAuthRoot.heightProperty());
        particles.setMouseTransparent(true);
        preAuthRoot.getChildren().add(0, particles);
    }
}
