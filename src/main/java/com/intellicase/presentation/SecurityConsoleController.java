package com.intellicase.presentation;

import com.intellicase.application.CaseController;
import com.intellicase.application.SecurityController;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for SecurityConsole.fxml.
 * Delegates to SecurityController (UC-07, UC-08, UC-09) and
 * CaseController (UC-15) from the application layer.
 */
public class SecurityConsoleController {
    private static final String ACTOR_ID = "OMEGA-7";
    private final SecurityController securityController = new SecurityController();
    private final CaseController caseController = new CaseController();

    @FXML
    private TextField profileIdField;
    @FXML
    private TextField aliasField;
    @FXML
    private TextField rawDataField;
    @FXML
    private TextField shadowCaseIdField;
    @FXML
    private Label shadowResultLabel;
    @FXML
    private Button createShadowBtn;

    @FXML
    private Label lockdownResultLabel;
    @FXML
    private Button lockdownBtn;

    @FXML
    private TextField promoteAgentIdField;
    @FXML
    private Label promoteResultLabel;
    @FXML
    private Button promoteBtn;

    @FXML
    private TextField overrideCodeField;
    @FXML
    private Label deactivateResultLabel;
    @FXML
    private Button deactivateBtn;

    @FXML
    private void initialize() {
        AudioFeedbackManager.attachTo(createShadowBtn);
        AudioFeedbackManager.attachTo(lockdownBtn);
        AudioFeedbackManager.attachTo(promoteBtn);
        AudioFeedbackManager.attachTo(deactivateBtn);
        GuidanceOverlayManager.highlightNode(createShadowBtn,
            "Step 1 → Create a Shadow Profile for a confidential informant");
    }

    @FXML
    private void handleCreateShadowProfile() {
        String pid = profileIdField.getText().trim();
        String alias = aliasField.getText().trim();
        String raw = rawDataField.getText().trim();
        String cid = shadowCaseIdField.getText().trim();
        if (pid.isEmpty() || alias.isEmpty() || raw.isEmpty() || cid.isEmpty()) {
            showError(shadowResultLabel, "All fields are required.");
            return;
        }
        boolean success = securityController.createShadowProfile(pid, alias, raw, cid, ACTOR_ID);
        if (success) {
            showSuccess(shadowResultLabel, "Shadow Profile [" + pid + "] created. Data encrypted via AES-256.");
        } else {
            showError(shadowResultLabel, "Failed — system lockdown may be active.");
        }
    }

    @FXML
    private void handleActivateLockdown() {
        boolean success = securityController.activateAuditLockdown(ACTOR_ID);
        if (success) {
            showSuccess(lockdownResultLabel, "LOCKDOWN ACTIVATED — All write operations are now blocked.");
        } else {
            showError(lockdownResultLabel, "Lockdown is already active.");
        }
    }

    @FXML
    private void handlePromoteClearance() {
        String agentId = promoteAgentIdField.getText().trim();
        if (agentId.isEmpty()) {
            showError(promoteResultLabel, "Agent ID is required.");
            return;
        }
        boolean success = securityController.promoteSecurityClearance(agentId, ACTOR_ID);
        if (success) {
            showSuccess(promoteResultLabel, "Agent [" + agentId + "] clearance promoted.");
        } else {
            showError(promoteResultLabel, "Failed — agent not found or lockdown active.");
        }
    }

    @FXML
    private void handleDeactivateLockdown() {
        String code = overrideCodeField.getText().trim();
        if (code.isEmpty()) {
            showError(deactivateResultLabel, "Override code is required.");
            return;
        }
        boolean success = caseController.deactivateAuditLockdown(code, ACTOR_ID);
        if (success) {
            showSuccess(deactivateResultLabel, "LOCKDOWN DEACTIVATED — Write operations restored.");
        } else {
            showError(deactivateResultLabel, "Override denied — invalid code.");
        }
    }

    private void showSuccess(Label label, String msg) {
        label.setText("✓ " + msg);
        label.setStyle("-fx-text-fill: #00ff88;");
        AudioFeedbackManager.playClick();
    }

    private void showError(Label label, String msg) {
        label.setText("✗ " + msg);
        label.setStyle("-fx-text-fill: #ff4444;");
        AudioFeedbackManager.playError();
    }
}
