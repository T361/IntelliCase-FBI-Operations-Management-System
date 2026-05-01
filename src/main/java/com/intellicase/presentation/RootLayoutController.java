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
    private Label agentLabel;

    @FXML
    private Label roleLabel;

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

    @FXML
    private Button navLockdown;

    private com.intellicase.presentation.effects.HyperParticleEngine particleEngine;

    @FXML
    private void initialize() {
        initParticleEngine();
        initViewRouter();
        initClock();
        refreshLockdownStatus();
        refreshUserInfo();
        applyRoleBasedAccess();
        applyMotion();
        ViewRouter.getInstance().navigateTo("/ui/MainMenu.fxml");
        setActiveButton(navMainMenu);
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
        ViewRouter.getInstance().setOnNavigation(this::syncSidebar);
    }

    private void syncSidebar() {
        String view = ViewRouter.getInstance().getCurrentView();
        if (view == null) {
            return;
        }
        if (view.contains("MainMenu")) {
            setActiveButton(navMainMenu);
        } else if (view.contains("SecurityConsole")) {
            setActiveButton(navSecurity);
        } else if (view.contains("EvidenceVault")) {
            setActiveButton(navEvidence);
        } else if (view.contains("CaseDashboard")) {
            setActiveButton(navCases);
        } else if (view.contains("LockdownControl")) {
            setActiveButton(navLockdown);
        }
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

    private void refreshUserInfo() {
        SessionManager sm = SessionManager.getInstance();
        if (sm.isLoggedIn()) {
            agentLabel.setText("Active Agent: " + sm.getDisplayName());
            roleLabel.setText(sm.getRole().replace('_', ' '));
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        AppStageManager.showLanding();
    }

    private void applyRoleBasedAccess() {
        String role = SessionManager.getInstance().getRole();
        // FBI_DIRECTOR: all visible (default)
        // CASE_SUPERVISOR: no lockdown control
        // FIELD_AGENT: no security console, no lockdown
        switch (role) {
            case "FIELD_AGENT":
                navSecurity.setVisible(false);
                navSecurity.setManaged(false);
                navLockdown.setVisible(false);
                navLockdown.setManaged(false);
                break;
            case "CASE_SUPERVISOR":
                navLockdown.setVisible(false);
                navLockdown.setManaged(false);
                break;
            case "INTELLIGENCE_ANALYST":
            case "FORENSIC_SPECIALIST":
                navLockdown.setVisible(false);
                navLockdown.setManaged(false);
                break;
            default:
                break;
        }
    }

    private void refreshLockdownStatus() {
        boolean active = SystemState.getInstance().isLockdownActive();
        lockdownStatusLabel.setText(active ? "LOCKDOWN: ACTIVE" : "LOCKDOWN: INACTIVE");
        lockdownStatusLabel.setStyle(active
            ? "-fx-text-fill: #ff4444; -fx-border-color: #ff4444;"
            : "-fx-text-fill: #C9A94D; -fx-border-color: rgba(201,169,77,0.5);");
        threatLabel.setText(active ? "Threat: CRITICAL" : "Threat: Low");
        threatLabel.setStyle(active
            ? "-fx-text-fill: #ff4444;"
            : "-fx-text-fill: #C9A94D;");
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
        setActiveButton(navMainMenu);
        refreshLockdownStatus();
    }

    @FXML
    private void goSecurity() {
        ViewRouter.getInstance().navigateTo("/ui/SecurityConsole.fxml");
        setActiveButton(navSecurity);
        com.intellicase.presentation.effects.HUDGuidanceOverlay.clear();
        refreshLockdownStatus();
    }

    @FXML
    private void goEvidence() {
        ViewRouter.getInstance().navigateTo("/ui/EvidenceVault.fxml");
        setActiveButton(navEvidence);
        com.intellicase.presentation.effects.HUDGuidanceOverlay.clear();
        refreshLockdownStatus();
    }

    @FXML
    private void goLockdown() {
        ViewRouter.getInstance().navigateTo("/ui/LockdownControl.fxml");
        setActiveButton(navLockdown);
        com.intellicase.presentation.effects.HUDGuidanceOverlay.clear();
        refreshLockdownStatus();
    }

    @FXML
    private void goCases() {
        ViewRouter.getInstance().navigateTo("/ui/CaseDashboard.fxml");
        setActiveButton(navCases);
        com.intellicase.presentation.effects.HUDGuidanceOverlay.clear();
        refreshLockdownStatus();
    }

    private void setActiveButton(Button active) {
        navMainMenu.getStyleClass().remove("nav-button-active");
        navSecurity.getStyleClass().remove("nav-button-active");
        navLockdown.getStyleClass().remove("nav-button-active");
        navEvidence.getStyleClass().remove("nav-button-active");
        navCases.getStyleClass().remove("nav-button-active");
        active.getStyleClass().add("nav-button-active");
    }
}
