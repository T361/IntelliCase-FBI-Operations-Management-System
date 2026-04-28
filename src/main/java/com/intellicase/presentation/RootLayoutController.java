package com.intellicase.presentation;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.intellicase.application.SystemState;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Controller for RootLayout.fxml. Manages sidebar navigation,
 * topbar status updates, and particle engine initialization.
 */
@SuppressWarnings("unused")
public class RootLayoutController {
    @FXML
    private StackPane rootStack;

    @FXML
    private BorderPane mainBorder;

    @FXML
    private StackPane contentPane;

    @FXML
    private Label timeLabel;

    @FXML
    private Label threatLabel;

    @FXML
    private Label lockdownStatusLabel;

    @FXML
    private Button navMainMenu;

    @FXML
    private Button navSecurity;

    @FXML
    private Button navEvidence;

    @FXML
    private Button navCases;

    private com.intellicase.presentation.effects.HyperParticleEngine particleEngine;

    @FXML
    private void initialize() {
        initParticleEngine();
        initViewRouter();
        initClock();
        refreshLockdownStatus();
        applyMotion();
        ViewRouter.getInstance().navigateTo("/ui/MainMenu.fxml");
        Platform.runLater(this::initGuidance);
        Platform.runLater(() -> AudioFeedbackManager.bindToScene(rootStack.getScene()));
    }

    private void initParticleEngine() {
        particleEngine = new com.intellicase.presentation.effects.HyperParticleEngine();
        particleEngine.widthProperty().bind(rootStack.widthProperty());
        particleEngine.heightProperty().bind(rootStack.heightProperty());
        particleEngine.setMouseTransparent(true);
        rootStack.getChildren().add(0, particleEngine);
    }

    private void initViewRouter() {
        ViewRouter.getInstance().setContentPane(contentPane);
    }

    private void initClock() {
        updateTime();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            updateTime();
            refreshLockdownStatus();
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void updateTime() {
        timeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private void refreshLockdownStatus() {
        boolean active = SystemState.getInstance().isLockdownActive();
        lockdownStatusLabel.setText(active ? "LOCKDOWN: ACTIVE" : "LOCKDOWN: INACTIVE");
        lockdownStatusLabel.setStyle(active
            ? "-fx-text-fill: #ff4444; -fx-border-color: #ff4444;"
            : "-fx-text-fill: #00f3ff; -fx-border-color: #00f3ff;");
        threatLabel.setText(active ? "Threat: CRITICAL" : "Threat: Low");
        threatLabel.setStyle(active
            ? "-fx-text-fill: #ff4444;"
            : "-fx-text-fill: #00ff88;");
    }

    private void initGuidance() {
        com.intellicase.presentation.effects.HUDGuidanceOverlay.highlight(navSecurity,
            "Start here → Open Security Ops to manage profiles & lockdowns");
    }

    @FXML
    private void applyMotion() {
        com.intellicase.presentation.effects.AnimationUtils.applyFloating(mainBorder);
        com.intellicase.presentation.effects.AnimationUtils.applyBreathing(navMainMenu);
    }

    @FXML
    private void goMainMenu() {
        ViewRouter.getInstance().navigateTo("/ui/MainMenu.fxml");
        refreshLockdownStatus();
    }

    @FXML
    private void goSecurity() {
        ViewRouter.getInstance().navigateTo("/ui/SecurityConsole.fxml");
        com.intellicase.presentation.effects.HUDGuidanceOverlay.clear();
        refreshLockdownStatus();
    }

    @FXML
    private void goEvidence() {
        ViewRouter.getInstance().navigateTo("/ui/EvidenceVault.fxml");
        com.intellicase.presentation.effects.HUDGuidanceOverlay.clear();
        refreshLockdownStatus();
    }

    @FXML
    private void goCases() {
        ViewRouter.getInstance().navigateTo("/ui/CaseDashboard.fxml");
        com.intellicase.presentation.effects.HUDGuidanceOverlay.clear();
        refreshLockdownStatus();
    }
}
