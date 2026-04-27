package com.intellicase.presentation;

import java.util.List;

import com.intellicase.application.CaseController;
import com.intellicase.domain.CaseFile;
import com.intellicase.domain.Evidence;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for CaseDashboard.fxml.
 * Delegates to CaseController for UC-11 and UC-14.
 */
public class CaseDashboardController {
    private static final String ACTOR_ID = "OMEGA-7";
    private final CaseController caseController = new CaseController();

    @FXML
    private TextField caseStatusField;
    @FXML
    private TextField casePriorityField;
    @FXML
    private TextField caseLocationField;
    @FXML
    private TextArea caseDescriptionArea;
    @FXML
    private Label caseResultLabel;
    @FXML
    private Button createCaseBtn;

    @FXML
    private TextField inventoryAgentIdField;
    @FXML
    private Button inventoryBtn;

    @FXML
    private TableView<Evidence> inventoryTable;
    @FXML
    private TableColumn<Evidence, String> invEvidenceIdColumn;
    @FXML
    private TableColumn<Evidence, String> invCaseIdColumn;
    @FXML
    private TableColumn<Evidence, String> invStatusColumn;
    @FXML
    private TableColumn<Evidence, String> invCustodianColumn;
    @FXML
    private TableColumn<Evidence, String> invHashColumn;

    @FXML
    private void initialize() {
        configureInventoryTable();
        AudioFeedbackManager.attachTo(createCaseBtn);
        AudioFeedbackManager.attachTo(inventoryBtn);
        GuidanceOverlayManager.highlightNode(createCaseBtn,
            "Create a new Smart Case with factory-generated identifiers");
    }

    private void configureInventoryTable() {
        invEvidenceIdColumn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getEvidenceId()));
        invCaseIdColumn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getCaseId()));
        invStatusColumn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getStatus()));
        invCustodianColumn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getCustodian()));
        invHashColumn.setCellValueFactory(d ->
            new SimpleStringProperty(d.getValue().getIntegrityHash()));
    }

    @FXML
    private void handleCreateSmartCase() {
        String status = caseStatusField.getText().trim();
        String desc = caseDescriptionArea.getText().trim();
        String priority = casePriorityField.getText().trim();
        String location = caseLocationField.getText().trim();
        if (status.isEmpty() || desc.isEmpty() || priority.isEmpty() || location.isEmpty()) {
            showError(caseResultLabel, "All fields are required.");
            return;
        }
        CaseFile created = caseController.createSmartCase(status, desc, priority, location, ACTOR_ID);
        if (created != null) {
            showSuccess(caseResultLabel, "Smart Case [" + created.getCaseId() + "] initialized.");
        } else {
            showError(caseResultLabel, "Failed — lockdown may be active.");
        }
    }

    @FXML
    private void handleEvidenceInventory() {
        String agentId = inventoryAgentIdField.getText().trim();
        if (agentId.isEmpty()) {
            AudioFeedbackManager.playError();
            return;
        }
        List<Evidence> inventory = caseController.secureEvidenceAudit(agentId);
        inventoryTable.setItems(FXCollections.observableArrayList(inventory));
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
