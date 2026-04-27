package com.intellicase.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Controller for MainMenu.fxml — the landing page with navigation cards
 * and system health telemetry.
 */
public class MainMenuController {
    @FXML
    private VBox securityCard;

    @FXML
    private VBox evidenceCard;

    @FXML
    private VBox caseCard;

    @FXML
    private Label casesCountLabel;

    @FXML
    private Label evidenceCountLabel;

    @FXML
    private Label agentsCountLabel;

    @FXML
    private Label alertsCountLabel;

    @FXML
    private ProgressBar readinessBar;

    @FXML
    private ProgressBar integrityBar;

    @FXML
    private ProgressBar signalBar;

    @FXML
    private void initialize() {
        AudioFeedbackManager.attachTo(securityCard);
        AudioFeedbackManager.attachTo(evidenceCard);
        AudioFeedbackManager.attachTo(caseCard);
    }

    @FXML
    private void goSecurity(MouseEvent event) {
        ViewRouter.getInstance().navigateTo("/ui/SecurityConsole.fxml");
    }

    @FXML
    private void goEvidence(MouseEvent event) {
        ViewRouter.getInstance().navigateTo("/ui/EvidenceVault.fxml");
    }

    @FXML
    private void goCases(MouseEvent event) {
        ViewRouter.getInstance().navigateTo("/ui/CaseDashboard.fxml");
    }
}
