package com.intellicase.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * Controller for MainMenu.fxml — landing page after authentication.
 * Displays live DB counters and a role-aware welcome message.
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
    private Label welcomeLabel;

    @FXML
    private void initialize() {
        AudioFeedbackManager.attachTo(securityCard);
        AudioFeedbackManager.attachTo(evidenceCard);
        AudioFeedbackManager.attachTo(caseCard);
        refreshCounts();
        refreshWelcome();
        applyRoleVisibility();
    }

    private void applyRoleVisibility() {
        String role = SessionManager.getInstance().getRole();
        if ("FIELD_AGENT".equals(role)) {
            securityCard.setVisible(false);
            securityCard.setManaged(false);
        }
    }

    private void refreshWelcome() {
        SessionManager sm = SessionManager.getInstance();
        String name = sm.isLoggedIn() ? sm.getDisplayName() : "Agent";
        String role = sm.isLoggedIn() ? sm.getRole().replace('_', ' ') : "";
        welcomeLabel.setText("Welcome back, " + name
            + (role.isEmpty() ? "." : " \u00b7 " + role));
    }

    private void refreshCounts() {
        casesCountLabel.setText(
            String.valueOf(new com.intellicase.dao.CaseFileDao().count()));
        evidenceCountLabel.setText(
            String.valueOf(new com.intellicase.dao.EvidenceDao().count()));
        agentsCountLabel.setText(
            String.valueOf(new com.intellicase.dao.AgentDao().count()));
        alertsCountLabel.setText(
            String.valueOf(new com.intellicase.dao.AuditLogDao().count()));
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
