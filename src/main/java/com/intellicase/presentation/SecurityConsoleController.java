package com.intellicase.presentation;

import com.intellicase.application.SecurityController;
import com.intellicase.application.SecurityController.LockdownAuthResult;
import com.intellicase.application.SystemState;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

/**
 * Controller for SecurityConsole.fxml — UC-08 (Activate Lockdown),
 * UC-07 (Shadow Profile), UC-09 (Promote Clearance).
 */
public class SecurityConsoleController {

    private static final String ACTOR_ID = "OMEGA-7";
    private final SecurityController securityController = new SecurityController();

    // Status banner
    @FXML private VBox lockdownBanner;
    @FXML private Circle statusDot;
    @FXML private Label lockdownStatusTitle;
    @FXML private Label lockdownStatusSub;
    @FXML private Label lockdownTimestampLabel;
    @FXML private Label lockdownActorLabel;

    // UC-08 Step 1
    @FXML private Button upsCheckBtn;
    @FXML private Label upsStatusLabel;

    // UC-08 Step 2
    @FXML private TextField directorIdField;
    @FXML private PasswordField directorPasswordField;

    // UC-08 Step 3
    @FXML private Button lockdownBtn;
    @FXML private Label lockdownResultLabel;
    @FXML private VBox dojPanel;
    @FXML private Label dojTimestampLabel;
    @FXML private VBox lockdownPanel;

    // UC-07 fields
    @FXML private TextField profileIdField;
    @FXML private TextField aliasField;
    @FXML private TextField rawDataField;
    @FXML private TextField shadowCaseIdField;
    @FXML private Label shadowResultLabel;
    @FXML private Button createShadowBtn;

    // UC-07 Extensions (E1, E3) fields
    @FXML private TextField viewProfileIdField;
    @FXML private TextField viewAgentIdField;
    @FXML private Button viewShadowBtn;
    @FXML private CheckBox identityVerifiedCheck;
    @FXML private Button linkFinancialBtn;

    // UC-09
    @FXML private TextField promoteAgentIdField;
    @FXML private Label promoteResultLabel;
    @FXML private Button promoteBtn;

    private boolean upsCheckPassed = false;

    @FXML
    private void initialize() {
        AudioFeedbackManager.attachTo(createShadowBtn);
        AudioFeedbackManager.attachTo(lockdownBtn);
        AudioFeedbackManager.attachTo(promoteBtn);
        AudioFeedbackManager.attachTo(viewShadowBtn);
        AudioFeedbackManager.attachTo(linkFinancialBtn);

        String role = SessionManager.getInstance().getRole();
        if (!"FBI_DIRECTOR".equals(role)) {
            lockdownPanel.setVisible(false);
            lockdownPanel.setManaged(false);
            lockdownBanner.setVisible(false);
            lockdownBanner.setManaged(false);
        } else {
            // Auto-run UPS check on open for Director
            handleUpsCheck();
            refreshStatusBanner();
            if (SystemState.getInstance().isLockdownActive()) {
                showDojPanel();
            }
        }
    }

    // ── UC-08 Step 1: UPS Check ───────────────────────────────────────────────

    @FXML
    private void handleUpsCheck() {
        upsCheckPassed = securityController.performUpsCheck();
        if (upsCheckPassed) {
            upsStatusLabel.setText("✓  PASSED — UPS ONLINE");
            upsStatusLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-weight: bold;");
            upsCheckBtn.setText("✓  UPS CHECK PASSED — RE-RUN");
        } else {
            upsStatusLabel.setText("✗  FAILED — CANNOT PROCEED");
            upsStatusLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
        }
    }

    // ── UC-08 Step 3: Activate Lockdown ──────────────────────────────────────

    @FXML
    private void handleActivateLockdown() {
        // Already locked?
        if (SystemState.getInstance().isLockdownActive()) {
            showLabel(lockdownResultLabel,
                "⚠  LOCKDOWN ALREADY ACTIVE — Navigate to 'Lockdown Control' to deactivate.",
                "#ffaa00");
            return;
        }
        // Require Step 1
        if (!upsCheckPassed) {
            showLabel(lockdownResultLabel,
                "✗  Step 1 Required: UPS check must pass before initiating lockdown.", "#ff4444");
            return;
        }
        // Validate Director credentials (Step 2)
        String dirId = directorIdField.getText().trim();
        String dirPass = directorPasswordField.getText().trim();
        if (dirId.isEmpty() || dirPass.isEmpty()) {
            showLabel(lockdownResultLabel,
                "✗  Step 2 Required: Enter Director ID and password.", "#ff4444");
            return;
        }

        LockdownAuthResult authResult = securityController.authenticateDirector(dirId, dirPass);
        switch (authResult) {
            case IDENTITY_FAILED:
                // E1: biometric/ID failure
                showLabel(lockdownResultLabel,
                    "✗  E1: IDENTITY VERIFICATION FAILED — IT Security has been alerted.",
                    "#ff4444");
                return;
            case CREDENTIAL_FAILED:
                // E1: wrong password
                showLabel(lockdownResultLabel,
                    "✗  E1: INVALID CREDENTIALS — Access denied. Incident logged.", "#ff4444");
                return;
            case PASSWORD_EXPIRED:
                // E3: expired password
                showLabel(lockdownResultLabel,
                    "⚠  E3: DIRECTOR PASSWORD EXPIRED — MFA reset required. Lockdown paused.",
                    "#ffaa00");
                return;
            default:
                break;
        }

        // Execute lockdown
        boolean success = securityController.activateAuditLockdown(dirId);
        if (success) {
            showLabel(lockdownResultLabel,
                "✓  LOCKDOWN ACTIVATED — All write operations globally suspended.", "#ff4444");
            showDojPanel();
            refreshStatusBanner();
        } else {
            showLabel(lockdownResultLabel, "✗  Lockdown failed unexpectedly.", "#ff4444");
        }
    }

    private void showDojPanel() {
        String ts = SystemState.getInstance().getLockdownTimestamp();
        dojPanel.setVisible(true);
        dojPanel.setManaged(true);
        dojTimestampLabel.setText(
            "Freeze timestamp: " + (ts != null ? ts : "N/A")
            + "  |  Reported to: doj-audit@fbi.gov");
    }

    // ── UC-07: Create Shadow Profile ─────────────────────────────────────────

    @FXML
    private void handleCreateShadowProfile() {
        String pid = profileIdField.getText().trim();
        String alias = aliasField.getText().trim();
        String raw = rawDataField.getText().trim();
        String cid = shadowCaseIdField.getText().trim();
        if (pid.isEmpty() || alias.isEmpty() || raw.isEmpty() || cid.isEmpty()) {
            showLabel(shadowResultLabel, "✗  All fields are required.", "#ff4444");
            return;
        }
        
        SecurityController.ShadowProfileResult result = securityController.createShadowProfile(pid, alias, raw, cid, ACTOR_ID);
        
        switch (result) {
            case SUCCESS:
                showLabel(shadowResultLabel,
                    "✓  Profile [" + pid + "] encrypted via AES-256-SIM.", "#00ff88");
                profileIdField.clear();
                aliasField.clear();
                rawDataField.clear();
                shadowCaseIdField.clear();
                break;
            case ALIAS_IN_USE:
                // E2: Alias already in use
                showLabel(shadowResultLabel, "✗ E2: Alias already in use. Generate a unique alias.", "#ffaa00");
                break;
            case PROFILE_EXISTS:
                showLabel(shadowResultLabel, "✗ Profile ID already exists in the system.", "#ff4444");
                break;
            case CASE_NOT_FOUND:
                showLabel(shadowResultLabel, "✗ Linked Case ID does not exist in the database.", "#ff4444");
                break;
            case LOCKDOWN_ACTIVE:
                showLabel(shadowResultLabel, "✗ Blocked — system lockdown is active.", "#ff4444");
                break;
        }
    }

    @FXML
    private void handleViewShadowProfile() {
        String pid = viewProfileIdField.getText().trim();
        String agent = viewAgentIdField.getText().trim();
        if (pid.isEmpty() || agent.isEmpty()) {
            showLabel(shadowResultLabel, "✗  Provide Profile ID and your Agent ID to view.", "#ffaa00");
            return;
        }

        String details = securityController.viewShadowProfile(pid, agent);
        if (details.startsWith("ACCESS DENIED")) {
            // E1: Unauthorized Agent attempts view
            showLabel(shadowResultLabel, "✗ E1: " + details, "#ff4444");
        } else if (details.startsWith("Profile not found")) {
            showLabel(shadowResultLabel, "✗ " + details, "#ffaa00");
        } else {
            showLabel(shadowResultLabel, "✓ " + details, "#00ff88");
        }
    }

    @FXML
    private void handleLinkFinancials() {
        String pid = viewProfileIdField.getText().trim();
        if (pid.isEmpty()) {
            showLabel(shadowResultLabel, "✗  Enter a Profile ID in the View field above.", "#ffaa00");
            return;
        }
        boolean verified = identityVerifiedCheck.isSelected();
        boolean linked = securityController.linkFinancials(pid, verified);
        
        if (linked) {
            showLabel(shadowResultLabel, "✓ Secure financial payout linked for " + pid, "#00ff88");
        } else {
            // E3: Informant identity verification fails -> prevents linking
            showLabel(shadowResultLabel, "✗ E3: Informant identity not verified. Payout linking blocked.", "#ff4444");
        }
    }

    // ── UC-09: Promote Clearance ──────────────────────────────────────────────

    @FXML
    private void handlePromoteClearance() {
        String agentId = promoteAgentIdField.getText().trim();
        if (agentId.isEmpty()) {
            showLabel(promoteResultLabel, "✗  Agent ID is required.", "#ff4444");
            return;
        }
        boolean ok = securityController.promoteSecurityClearance(agentId, ACTOR_ID);
        if (ok) {
            showLabel(promoteResultLabel,
                "✓  Agent [" + agentId + "] clearance level promoted.", "#00ff88");
        } else {
            showLabel(promoteResultLabel,
                "✗  Failed — agent not found or lockdown is active.", "#ff4444");
        }
    }

    // ── Status Banner ─────────────────────────────────────────────────────────

    private void refreshStatusBanner() {
        boolean active = SystemState.getInstance().isLockdownActive();
        if (active) {
            lockdownBanner.getStyleClass().setAll("lockdown-banner-active");
            lockdownStatusTitle.setText(
                "⚠  SYSTEM STATUS: LOCKDOWN ACTIVE — READ-ONLY MODE");
            lockdownStatusTitle.getStyleClass().setAll("lockdown-status-title-active");
            lockdownStatusSub.setText(
                "All data modification privileges are globally suspended.");
            String ts = SystemState.getInstance().getLockdownTimestamp();
            String actor = SystemState.getInstance().getLockdownActorId();
            lockdownTimestampLabel.setText("Activated: " + (ts != null ? ts : "N/A"));
            lockdownActorLabel.setText("By: " + (actor != null ? actor : "UNKNOWN"));
        } else {
            lockdownBanner.getStyleClass().setAll("lockdown-banner-normal");
            lockdownStatusTitle.setText("SYSTEM STATUS: NORMAL OPERATIONAL PARAMETERS");
            lockdownStatusTitle.getStyleClass().setAll("lockdown-status-title-normal");
            lockdownStatusSub.setText(
                "All write operations are enabled. No active audit freeze.");
            lockdownTimestampLabel.setText("");
            lockdownActorLabel.setText("");
        }
    }

    private void showLabel(Label label, String msg, String color) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
        
        // Ensure message is fully visible on hover
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(msg);
        tooltip.setShowDelay(javafx.util.Duration.millis(10));
        tooltip.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-background-color: #222222; -fx-padding: 5px;");
        label.setTooltip(tooltip);
    }
}
