package com.intellicase.presentation;

import java.util.List;

import com.intellicase.application.EvidenceController;
import com.intellicase.domain.AuditLogEntry;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import com.intellicase.dao.CaseFileDao;
import com.intellicase.domain.CaseFile;

/**
 * Controller for EvidenceVault.fxml.
 * Delegates to EvidenceController for UC-01, UC-03, UC-05.
 */
public class EvidenceVaultController {
    private static final String ACTOR_ID = "AGT-7788-9900";
    private final EvidenceController evidenceController = new EvidenceController();

    @FXML
    private TextField handshakeEvidenceIdField;
    @FXML
    private TextField handshakeRecipientIdField;
    @FXML
    private TextField handshakeSignatureField;
    @FXML
    private Label handshakeResultLabel;
    @FXML
    private Button handshakeBtn;

    @FXML
    private TextField loadAgentIdField;
    @FXML
    private MenuButton loadCaseMenuButton;
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
        populateCaseMenu();
        AudioFeedbackManager.attachTo(handshakeBtn);
        AudioFeedbackManager.attachTo(loadScoreBtn);
        AudioFeedbackManager.attachTo(auditTrailBtn);
        GuidanceOverlayManager.highlightNode(handshakeBtn,
            "Initiate a digital handshake to secure evidence transfer");
    }

    private void populateCaseMenu() {
        CaseFileDao caseDao = new CaseFileDao();
        List<CaseFile> cases = caseDao.findAll();
        loadCaseMenuButton.getItems().clear();
        for (CaseFile c : cases) {
            CheckMenuItem item = new CheckMenuItem(c.getCaseId());
            item.setOnAction(e -> updateMenuButtonText());
            loadCaseMenuButton.getItems().add(item);
        }
    }

    private void updateMenuButtonText() {
        int count = 0;
        for (MenuItem item : loadCaseMenuButton.getItems()) {
            if (item instanceof CheckMenuItem && ((CheckMenuItem) item).isSelected()) {
                count++;
            }
        }
        if (count == 0) {
            loadCaseMenuButton.setText("Cases to Assign");
        } else {
            loadCaseMenuButton.setText(count + " Selected");
        }
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
        String recipientId = handshakeRecipientIdField.getText().trim();
        String signature = handshakeSignatureField.getText().trim();

        if (evidenceId.isEmpty() || recipientId.isEmpty() || signature.isEmpty()) {
            showError(handshakeResultLabel, "All fields (Evidence ID, Recipient ID, Signature) are required.");
            return;
        }

        EvidenceController.HandshakeResult result = evidenceController.initiateDigitalHandshake(
            evidenceId, recipientId, signature, ACTOR_ID);
        switch (result) {
            case SUCCESS:
                showSuccess(handshakeResultLabel, "Digital handshake complete. [" + evidenceId + "] Status → IN_TRANSIT.");
                break;
            case NOT_FOUND:
                showError(handshakeResultLabel, "Evidence or Recipient Agent not found.");
                break;
            case UNAUTHORIZED:
                showError(handshakeResultLabel, "E1: Recipient lacks required security clearance. Access Denied.");
                break;
            case ALREADY_IN_TRANSIT:
                showError(handshakeResultLabel, "E2: Evidence is already IN_TRANSIT or locked.");
                break;
            case LOCKDOWN_ACTIVE:
                showError(handshakeResultLabel, "E3: System-Wide Audit Lockdown is active. Transfer blocked.");
                break;
            case INVALID_SIGNATURE:
                showError(handshakeResultLabel, "Digital signature is invalid.");
                break;
        }
    }

    @FXML
    private void handleLoadScore() {
        String agentId = loadAgentIdField.getText().trim();
        
        int caseCount = 0;
        for (MenuItem item : loadCaseMenuButton.getItems()) {
            if (item instanceof CheckMenuItem && ((CheckMenuItem) item).isSelected()) {
                caseCount++;
            }
        }

        if (agentId.isEmpty() || caseCount == 0) {
            showError(loadScoreResultLabel, "Agent ID is required and at least 1 case must be selected.");
            return;
        }

        boolean success = evidenceController.assignAgentSmartLoad(agentId, caseCount, ACTOR_ID);
        if (success) {
            showSuccess(loadScoreResultLabel, "Agent [" + agentId + "] load score updated with " + caseCount + " cases.");
        } else {
            showError(loadScoreResultLabel, "Failed — agent not found, threshold exceeded, or lockdown active.");
        }
    }

    @FXML
    private Label watermarkLabel;
    
    @FXML
    private Label auditResultLabel;

    @FXML
    private void handleViewAuditTrail() {
        String targetId = auditTargetIdField.getText().trim();
        if (targetId.isEmpty()) {
            AudioFeedbackManager.playError();
            return;
        }

        EvidenceController.AuditTrailResult result = evidenceController.viewEvidenceAuditTrail(targetId, ACTOR_ID);

        switch (result.getType()) {
            case SUCCESS:
                auditTable.setItems(FXCollections.observableArrayList(result.getLogs()));
                if (watermarkLabel != null) {
                    watermarkLabel.setText(result.getWatermark());
                    watermarkLabel.setVisible(true);
                }
                showSuccess(auditResultLabel, "Audit Trail generated successfully.");
                System.out.println("[UC-03] Report compiled within 2 seconds. Export watermark: " + result.getWatermark());
                AudioFeedbackManager.playClick();
                break;
            case NOT_FOUND:
                auditTable.getItems().clear();
                showError(auditResultLabel, "E3: Target evidence ID [" + targetId + "] does not exist.");
                AudioFeedbackManager.playError();
                System.out.println("[EvidenceAudit] E3: Evidence record could not be found.");
                break;
            case UNAUTHORIZED:
                auditTable.getItems().clear();
                showError(auditResultLabel, "E1: Automatic Notification of Security Breach. Access denied.");
                System.out.println("[EvidenceAudit] E1: Automatic Notification of Security Breach. Access denied.");
                AudioFeedbackManager.playError();
                break;
            case CORRUPTED:
                auditTable.getItems().clear();
                showError(auditResultLabel, "E2: Emergency Priority Escalation! Corrupted log data detected.");
                System.out.println("[EvidenceAudit] E2: Emergency Priority Escalation! Corrupted log data detected.");
                AudioFeedbackManager.playError();
                break;
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
