package com.intellicase.presentation;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.intellicase.application.SystemState;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * Controller for RootLayout.fxml. Manages sidebar navigation,
 * topbar status updates, and ParticleCanvas initialization.
 */
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

    private ParticleCanvas particleCanvas;

    @FXML
    private void initialize() {
        initParticleCanvas();
        initViewRouter();
        initClock();
        attachAudioFeedback();
        refreshLockdownStatus();
        ViewRouter.getInstance().navigateTo("/ui/MainMenu.fxml");
        initGuidance();
    }

    private void initParticleCanvas() {
        particleCanvas = new ParticleCanvas();
        particleCanvas.widthProperty().bind(rootStack.widthProperty());
        particleCanvas.heightProperty().bind(rootStack.heightProperty());
        particleCanvas.setMouseTransparent(true);
        rootStack.getChildren().add(0, particleCanvas);
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

    private void attachAudioFeedback() {
        AudioFeedbackManager.attachTo(navMainMenu);
        AudioFeedbackManager.attachTo(navSecurity);
        AudioFeedbackManager.attachTo(navEvidence);
        AudioFeedbackManager.attachTo(navCases);
    }

    private void initGuidance() {
        GuidanceOverlayManager.highlightNode(navSecurity,
            "Start here → Open Security Ops to manage profiles & lockdowns");
    }

    @FXML
    private void goMainMenu() {
        ViewRouter.getInstance().navigateTo("/ui/MainMenu.fxml");
        refreshLockdownStatus();
    }

    @FXML
    private void goSecurity() {
        ViewRouter.getInstance().navigateTo("/ui/SecurityConsole.fxml");
        GuidanceOverlayManager.clearHighlights();
        refreshLockdownStatus();
    }

    @FXML
    private void goEvidence() {
        ViewRouter.getInstance().navigateTo("/ui/EvidenceVault.fxml");
        GuidanceOverlayManager.clearHighlights();
        refreshLockdownStatus();
    }

    @FXML
    private void goCases() {
        ViewRouter.getInstance().navigateTo("/ui/CaseDashboard.fxml");
        GuidanceOverlayManager.clearHighlights();
        refreshLockdownStatus();
    }
}
