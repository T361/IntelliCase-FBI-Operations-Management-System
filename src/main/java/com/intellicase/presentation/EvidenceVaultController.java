package com.intellicase.presentation;

import java.util.List;

import com.intellicase.application.EvidenceController;
import com.intellicase.domain.AuditLogEntry;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Controller for EvidenceVault.fxml.
 * Delegates to EvidenceController for UC-01, UC-03, UC-05.
 */
public class EvidenceVaultController {
    private static final String ACTOR_ID = "OMEGA-7";
    private final EvidenceController evidenceController = new EvidenceController();

    @FXML
    private TextField handshakeEvidenceIdField;
    @FXML
    private Label handshakeResultLabel;
    @FXML
    private Button handshakeBtn;

    @FXML
    private TextField loadAgentIdField;
    @FXML
    private TextField activeCaseCountField;
    @FXML
    private Label loadScoreResultLabel;
    @FXML
    private Button loadScoreBtn;

    @FXML
    private TextField auditTargetIdField;
    @FXML
    private Button auditTrailBtn;

    @FXML
    private TableView<AuditLogEntry> auditTable;
    @FXML
    private TableColumn<AuditLogEntry, String> auditLogIdColumn;
    @FXML
    private TableColumn<AuditLogEntry, String> auditActionColumn;
    @FXML
    private TableColumn<AuditLogEntry, String> auditActorColumn;
    @FXML
    private TableColumn<AuditLogEntry, String> auditTimestampColumn;

    @FXML
    private void initialize() {
        configureAuditTable();
        AudioFeedbackManager.attachTo(handshakeBtn);
        AudioFeedbackManager.attachTo(loadScoreBtn);
        AudioFeedbackManager.attachTo(auditTrailBtn);
        GuidanceOverlayManager.highlightNode(handshakeBtn,
            "Initiate a digital handshake to secure evidence transfer");
    }

    private void configureAuditTable() {
        auditLogIdColumn.setCellValueFactory(d ->
            new SimpleStringProperty(String.valueOf(d.getValue().getLogId())));
        auditActionColumn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getAction()));
        auditActorColumn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getActorId()));
        auditTimestampColumn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getTimestamp() != null ? d.getValue().getTimestamp() : "—"));
    }

    @FXML
    private void handleDigitalHandshake() {
        String evidenceId = handshakeEvidenceIdField.getText().trim();
        if (evidenceId.isEmpty()) {
            showError(handshakeResultLabel, "Evidence ID is required.");
            return;
        }
        boolean success = evidenceController.initiateDigitalHandshake(evidenceId, ACTOR_ID);
        if (success) {
            showSuccess(handshakeResultLabel, "Digital handshake initiated for [" + evidenceId + "]. Status → IN_TRANSIT.");
        } else {
            showError(handshakeResultLabel, "Failed — evidence not found or lockdown active.");
        }
    }

    @FXML
    private void handleLoadScore() {
        String agentId = loadAgentIdField.getText().trim();
        String countStr = activeCaseCountField.getText().trim();
        if (agentId.isEmpty() || countStr.isEmpty()) {
            showError(loadScoreResultLabel, "Agent ID and case count are required.");
            return;
        }
        int caseCount;
        try {
            caseCount = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            showError(loadScoreResultLabel, "Case count must be a valid integer.");
            return;
        }
        boolean success = evidenceController.assignAgentSmartLoad(agentId, caseCount, ACTOR_ID);
        if (success) {
            showSuccess(loadScoreResultLabel, "Agent [" + agentId + "] load score updated.");
        } else {
            showError(loadScoreResultLabel, "Failed — agent not found, threshold exceeded, or lockdown active.");
        }
    }

    @FXML
    private void handleViewAuditTrail() {
        String targetId = auditTargetIdField.getText().trim();
        if (targetId.isEmpty()) {
            AudioFeedbackManager.playError();
            return;
        }
        List<AuditLogEntry> entries = evidenceController.viewAuditTrail(targetId);
        auditTable.setItems(FXCollections.observableArrayList(entries));
        AudioFeedbackManager.playClick();
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
