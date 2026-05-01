package com.intellicase.presentation;

import java.time.Instant;

import com.intellicase.application.CaseController;
import com.intellicase.application.SecurityController;
import com.intellicase.application.SystemState;
import com.intellicase.dao.AuditLogDao;
import com.intellicase.domain.AuditLogEntry;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller for LockdownControl.fxml — UC-15 Deactivate System Audit Lockdown.
 * Implements 3-step deactivation: ledger sync, MFA auth, override + confirm.
 * Covers extensions E1 (auth failure), E2 (node sync delay), E3 (unauthorized admin).
 */
public class LockdownControlController {

    private static final String ACTOR_ID = "OMEGA-7";
    // E3: admins below this level are blocked without Director co-auth

    private final CaseController caseController = new CaseController();
    private final AuditLogDao auditLogDao = new AuditLogDao();

    // Status banner
    @FXML private VBox lockdownBanner;
    @FXML private Label lockdownStatusTitle;
    @FXML private Label lockdownStatusSub;
    @FXML private Label lockdownTimestampLabel;
    @FXML private Label lockdownActorLabel;

    // Step 1
    @FXML private Button ledgerCheckBtn;
    @FXML private Label ledgerStatusLabel;

    // Step 2
    @FXML private TextField directorIdField;
    @FXML private PasswordField directorPasswordField;

    // Step 3
    @FXML private TextField overrideCodeField;
    @FXML private TextField confirmTextField;
    @FXML private Button deactivateBtn;
    @FXML private Label deactivateResultLabel;

    // Post-deactivation panel
    @FXML private VBox broadcastPanel;
    @FXML private Label broadcastTimestampLabel;

    private boolean ledgerSyncPassed = false;

    @FXML
    private void initialize() {
        AudioFeedbackManager.attachTo(deactivateBtn);
        AudioFeedbackManager.attachTo(ledgerCheckBtn);
        refreshStatusBanner();
    }

    // ── UC-15 Step 1: Ledger Sync Check (SR-3) ───────────────────────────────

    @FXML
    private void handleLedgerCheck() {
        // E2: Simulate distributed ledger check — verifies node consistency
        System.out.println("[UC-15] Running distributed ledger integrity check...");
        ledgerSyncPassed = true; // All nodes confirmed in simulated environment
        ledgerStatusLabel.setText("✓  LEDGER SYNCED — All nodes consistent");
        ledgerStatusLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-weight: bold;");
        ledgerCheckBtn.setText("✓  LEDGER SYNCED — RE-RUN");
        auditLogDao.create(new AuditLogEntry(
            "LEDGER_SYNC_CHECK", "SYSTEM", ACTOR_ID));
    }

    // ── UC-15 Step 3: Deactivate Lockdown ────────────────────────────────────

    @FXML
    private void handleDeactivateLockdown() {
        // Nothing to deactivate?
        if (!SystemState.getInstance().isLockdownActive()) {
            showLabel(deactivateResultLabel,
                "⚠  No active lockdown detected. System is already operational.", "#ffaa00");
            return;
        }

        // Step 1 gate
        if (!ledgerSyncPassed) {
            showLabel(deactivateResultLabel,
                "✗  Step 1 Required: Run Ledger Sync Check before deactivating.", "#ff4444");
            return;
        }

        // Step 2: MFA validation
        String dirId = directorIdField.getText().trim();
        String dirPass = directorPasswordField.getText().trim();
        if (dirId.isEmpty() || dirPass.isEmpty()) {
            showLabel(deactivateResultLabel,
                "✗  Step 2 Required: Enter Director ID and password.", "#ff4444");
            return;
        }

        // E3: Block non-Director admins (regular admin accounts start with "ADMIN-")
        if (dirId.startsWith("ADMIN-")) {
            auditLogDao.create(new AuditLogEntry(
                "DEACTIVATION_BLOCKED_E3", dirId, dirId));
            showLabel(deactivateResultLabel,
                "✗  E3: UNAUTHORIZED — Regular admins require Director co-authorization.",
                "#ff4444");
            return;
        }

        // E1: Check Director credentials via DB
        SecurityController securityController = new SecurityController();
        SecurityController.LockdownAuthResult authResult = securityController.authenticateDirector(dirId, dirPass);
        
        if (authResult != SecurityController.LockdownAuthResult.SUCCESS) {
            auditLogDao.create(new AuditLogEntry(
                "DEACTIVATION_AUTH_FAILED_E1", dirId, dirId));
            showLabel(deactivateResultLabel,
                "✗  E1: AUTHENTICATION FAILURE — Access denied or unauthorized role.",
                "#ff4444");
            return;
        }

        // Step 3: Override code
        String code = overrideCodeField.getText().trim();
        String confirm = confirmTextField.getText().trim();
        if (code.isEmpty()) {
            showLabel(deactivateResultLabel, "✗  Override code is required.", "#ff4444");
            return;
        }
        if (!"CONFIRM".equals(confirm)) {
            showLabel(deactivateResultLabel,
                "✗  Type CONFIRM exactly in the confirmation field.", "#ff4444");
            return;
        }

        // Execute deactivation
        boolean success = caseController.deactivateAuditLockdown(code, dirId);
        if (success) {
            String ts = Instant.now().toString();
            // SR-2: Global broadcast
            auditLogDao.create(new AuditLogEntry(
                "GLOBAL_BROADCAST_DEACTIVATION", "ALL_PERSONNEL", dirId));
            // Open Issues: DOJ notification
            auditLogDao.create(new AuditLogEntry(
                "DOJ_DEACTIVATION_NOTIFY", "DOJ-UNFREEZE-" + ts, dirId));

            showLabel(deactivateResultLabel,
                "✓  LOCKDOWN DEACTIVATED — Normal operations restored globally.", "#00ff88");

            // Show broadcast panel
            broadcastPanel.setVisible(true);
            broadcastPanel.setManaged(true);
            broadcastTimestampLabel.setText(
                "Deactivation timestamp: " + ts + "  |  Authorized by: " + dirId);

            refreshStatusBanner();
            // Reset fields
            overrideCodeField.clear();
            confirmTextField.clear();
            ledgerSyncPassed = false;
            ledgerStatusLabel.setText("PENDING...");
            ledgerStatusLabel.setStyle("");
            ledgerCheckBtn.setText("🔗  RUN LEDGER SYNC CHECK");
        } else {
            showLabel(deactivateResultLabel,
                "✗  Override code invalid. Deactivation denied.", "#ff4444");
        }
    }

    // ── Status Banner ─────────────────────────────────────────────────────────

    private void refreshStatusBanner() {
        boolean active = SystemState.getInstance().isLockdownActive();
        if (active) {
            lockdownBanner.getStyleClass().setAll("lockdown-banner-active");
            lockdownStatusTitle.setText(
                "⚠  SYSTEM STATUS: LOCKDOWN ACTIVE — DEACTIVATION REQUIRED");
            lockdownStatusTitle.getStyleClass().setAll("lockdown-status-title-active");
            lockdownStatusSub.setText(
                "All write operations are globally suspended. Follow steps below.");
            String ts = SystemState.getInstance().getLockdownTimestamp();
            String actor = SystemState.getInstance().getLockdownActorId();
            lockdownTimestampLabel.setText("Activated: " + (ts != null ? ts : "N/A"));
            lockdownActorLabel.setText("By: " + (actor != null ? actor : "UNKNOWN"));
        } else {
            lockdownBanner.getStyleClass().setAll("lockdown-banner-normal");
            lockdownStatusTitle.setText(
                "SYSTEM STATUS: NORMAL — LOCKDOWN NOT ACTIVE");
            lockdownStatusTitle.getStyleClass().setAll("lockdown-status-title-normal");
            lockdownStatusSub.setText(
                "No active lockdown detected. Nothing to deactivate.");
            lockdownTimestampLabel.setText("");
            lockdownActorLabel.setText("");
        }
    }

    private void showLabel(Label label, String msg, String color) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }
}
