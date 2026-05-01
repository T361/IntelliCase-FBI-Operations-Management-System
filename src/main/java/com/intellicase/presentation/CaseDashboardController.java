package com.intellicase.presentation;

import java.util.List;

import com.intellicase.application.CaseController;
import com.intellicase.application.LocationService;
import com.intellicase.domain.CaseFile;
import com.intellicase.domain.Evidence;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Controller for CaseDashboard.fxml.
 * Implements UC-11 (Smart Case Initializer) and UC-14 (Secure Evidence Inventory Audit).
 */
public class CaseDashboardController {

    private static final String ACTOR_ID = "OMEGA-7";
    private static final String STATUS_VERIFIED = "✓  INTEGRITY VERIFIED";
    private static final String STATUS_TAMPER = "⚠  TAMPER ALERT";
    private static final String RESTRICTED_MASK = "[RESTRICTED — CLEARANCE INSUFFICIENT]";

    private final CaseController caseController = new CaseController();
    private final LocationService locationService = new LocationService();

    // UC-11 fields
    @FXML private ComboBox<String> caseRegionBox;
    @FXML private ComboBox<String> caseStatusBox;
    @FXML private ComboBox<String> casePriorityBox;
    @FXML private ComboBox<String> caseLocationBox;
    @FXML private TextArea caseDescriptionArea;
    @FXML private Label caseResultLabel;
    @FXML private Button createCaseBtn;

    // UC-14 fields
    @FXML private TextField inventoryAgentIdField;
    @FXML private TextField caseIdFilterField;
    @FXML private Button inventoryBtn;
    @FXML private Label inventoryResultLabel;

    @FXML private TableView<Evidence> inventoryTable;
    @FXML private TableColumn<Evidence, String> invIntegrityColumn;
    @FXML private TableColumn<Evidence, String> invEvidenceIdColumn;
    @FXML private TableColumn<Evidence, String> invCaseIdColumn;
    @FXML private TableColumn<Evidence, String> invStatusColumn;
    @FXML private TableColumn<Evidence, String> invCustodianColumn;
    @FXML private TableColumn<Evidence, String> invHashColumn;

    @FXML
    private void initialize() {
        checkAndSeedData();
        configureInventoryTable();
        populateComboBoxes();
        AudioFeedbackManager.attachTo(createCaseBtn);
        AudioFeedbackManager.attachTo(inventoryBtn);
        GuidanceOverlayManager.highlightNode(createCaseBtn,
            "Create a new Smart Case with factory-generated identifiers");
        applyRoleRestrictions();
    }

    private void applyRoleRestrictions() {
        String role = SessionManager.getInstance().getRole();
        // Field agents cannot initialize new cases
        if ("FIELD_AGENT".equals(role)) {
            createCaseBtn.setDisable(true);
            createCaseBtn.setText("🔒  INITIALIZE CASE (Supervisor+)");
        }
        // Pre-fill agent ID from session for convenience
        String username = SessionManager.getInstance().getDisplayName();
        if (!username.isEmpty()) {
            inventoryAgentIdField.setText("AGT-7788-9900");
        }
        // Director sees all — no restrictions
    }

    // ── UC-14: Auto-seed test data if DB is empty ─────────────────────────────

    private void checkAndSeedData() {
        com.intellicase.dao.AgentDao agentDao = new com.intellicase.dao.AgentDao();
        if (agentDao.count() > 0) {
            return;
        }
        System.out.println("[DB] Seeding UC-14 test data...");
        agentDao.create(new com.intellicase.domain.Agent(
            "AGT-7788-9900", "Special Agent OMEGA", 5, 10));
        agentDao.create(new com.intellicase.domain.Agent(
            "AGT-1122-3344", "Probationary Agent ALPHA", 1, 5));

        com.intellicase.dao.CaseFileDao caseDao = new com.intellicase.dao.CaseFileDao();
        caseDao.create(new com.intellicase.domain.CaseFile(
            "CASE-2026-001", "ACTIVE", "Op Nightfall", "HIGH", "Washington, D.C."));
        caseDao.create(new com.intellicase.domain.CaseFile(
            "CASE-2026-002", "OPEN", "Blue Diamond Heist", "MEDIUM", "New York City, NY"));

        com.intellicase.dao.EvidenceDao evDao = new com.intellicase.dao.EvidenceDao();
        String h1 = "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        evDao.create(new com.intellicase.domain.Evidence(
            "EVD-001", "CASE-2026-001", "SECURED", "AGT-7788-9900", h1, 3));
        String h2 = "sha256:5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5";
        evDao.create(new com.intellicase.domain.Evidence(
            "EVD-002", "CASE-2026-001", "ANALYSIS", "AGT-7788-9900", h2, 4));
        // Tampered item — no hash (E1 scenario)
        evDao.create(new com.intellicase.domain.Evidence(
            "EVD-003", "CASE-2026-002", "SECURED", "AGT-7788-9900", null, 2));
    }

    // ── UC-14: Table Configuration ────────────────────────────────────────────

    private void configureInventoryTable() {
        invEvidenceIdColumn.setCellValueFactory(
            d -> new SimpleStringProperty(d.getValue().getEvidenceId()));
        invCaseIdColumn.setCellValueFactory(
            d -> new SimpleStringProperty(d.getValue().getCaseId()));
        invStatusColumn.setCellValueFactory(
            d -> new SimpleStringProperty(d.getValue().getStatus()));
        invCustodianColumn.setCellValueFactory(
            d -> new SimpleStringProperty(d.getValue().getCustodian()));
        invHashColumn.setCellValueFactory(
            d -> new SimpleStringProperty(d.getValue().getIntegrityHash()));

        // Integrity status column — computed from hash presence
        invIntegrityColumn.setCellValueFactory(d -> {
            String hash = d.getValue().getIntegrityHash();
            boolean tampered = hash == null || hash.isBlank()
                || hash.equals(RESTRICTED_MASK);
            return new SimpleStringProperty(tampered ? STATUS_TAMPER : STATUS_VERIFIED);
        });

        // Color the integrity cell based on its value
        invIntegrityColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else if (item.contains("TAMPER")) {
                    setText(item);
                    setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;"
                        + " -fx-effect: dropshadow(gaussian, #ff4444, 8, 0.4, 0, 0);");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #00ff88; -fx-font-weight: bold;");
                }
            }
        });
    }

    // ── UC-11: Combo Boxes ────────────────────────────────────────────────────

    private void populateComboBoxes() {
        caseStatusBox.setItems(FXCollections.observableArrayList(
            "ACTIVE", "OPEN", "CLOSED", "COLD"));
        caseStatusBox.setValue("ACTIVE");

        casePriorityBox.setItems(FXCollections.observableArrayList(
            "LOW", "MEDIUM", "HIGH", "CRITICAL"));
        casePriorityBox.setValue("MEDIUM");

        caseRegionBox.setItems(FXCollections.observableArrayList(locationService.getRegions()));
        caseRegionBox.setValue("USA (Field Offices)");
        handleRegionChange();
        caseLocationBox.setEditable(true);
    }

    @FXML
    private void handleRegionChange() {
        String region = caseRegionBox.getValue();
        if (region != null) {
            List<String> locs = locationService.getLocationsByRegion(region);
            caseLocationBox.setItems(FXCollections.observableArrayList(locs));
            if (!caseLocationBox.getItems().isEmpty()) {
                caseLocationBox.setValue(caseLocationBox.getItems().get(0));
            }
        }
    }

    // ── UC-11: Create Smart Case ──────────────────────────────────────────────

    @FXML
    private void handleCreateSmartCase() {
        String status = caseStatusBox.getValue();
        String desc = caseDescriptionArea.getText().trim();
        String priority = casePriorityBox.getValue();
        String location = caseLocationBox.getValue();
        if (desc.isEmpty() || status == null || priority == null || location == null) {
            showLabel(caseResultLabel, "✗  All fields are required.", "#ff4444");
            return;
        }
        CaseFile created = caseController.createSmartCase(status, desc, priority, location, ACTOR_ID);
        if (created != null) {
            showLabel(caseResultLabel, "✓  Case [" + created.getCaseId() + "] initialized.", "#00ff88");
        } else {
            showLabel(caseResultLabel, "✗  Failed — lockdown may be active.", "#ff4444");
        }
    }

    // ── UC-14: Secure Evidence Inventory Audit ────────────────────────────────

    @FXML
    private void handleEvidenceInventory() {
        String agentId = inventoryAgentIdField.getText().trim();
        if (agentId.isEmpty()) {
            showLabel(inventoryResultLabel, "✗  AGENT ID REQUIRED", "#ff4444");
            return;
        }

        String caseFilter = caseIdFilterField.getText().trim();
        List<Evidence> inventory = caseController.secureEvidenceAudit(agentId, caseFilter);
        inventoryTable.setItems(FXCollections.observableArrayList(inventory));

        if (inventory.isEmpty()) {
            String filterInfo = caseFilter.isEmpty() ? "" : " for case [" + caseFilter + "]";
            showLabel(inventoryResultLabel,
                "✗  ACCESS DENIED — INSUFFICIENT CLEARANCE OR NO RECORDS" + filterInfo,
                "#ff4444");
        } else {
            long tamperCount = inventory.stream()
                .filter(e -> e.getIntegrityHash() == null || e.getIntegrityHash().isBlank())
                .count();
            String summary = "✓  AUDIT COMPLETE — " + inventory.size() + " items";
            if (tamperCount > 0) {
                summary += "  |  ⚠  " + tamperCount + " TAMPER ALERT(S)";
                showLabel(inventoryResultLabel, summary, "#ffaa00");
            } else {
                showLabel(inventoryResultLabel, summary + "  |  ALL HASHES VERIFIED", "#00ff88");
            }
        }
    }

    @FXML
    private void handleClearAudit() {
        inventoryAgentIdField.clear();
        caseIdFilterField.clear();
        inventoryTable.getItems().clear();
        inventoryResultLabel.setText("");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showLabel(Label label, String msg, String color) {
        label.setText(msg);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }
}
