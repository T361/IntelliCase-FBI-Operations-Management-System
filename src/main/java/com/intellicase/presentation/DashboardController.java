package com.intellicase.presentation;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.intellicase.application.CaseController;
import com.intellicase.application.EvidenceController;
import com.intellicase.application.SecurityController;
import com.intellicase.application.SystemState;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Presentation controller for the JavaFX dashboard.
 * GRASP Controller: delegates actions to application controllers only.
 */
public class DashboardController {
    @FXML
    private Label lockdownStatusLabel;

    @FXML
    private Label workspaceTitleLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label threatLabel;

    @FXML
    private Label casesCountLabel;

    @FXML
    private Label evidenceCountLabel;

    @FXML
    private Label agentsCountLabel;

    @FXML
    private Label alertsCountLabel;

    @FXML
    private ListView<String> activityList;

    @FXML
    private TableView<EvidenceRow> evidenceTable;

    @FXML
    private TableColumn<EvidenceRow, String> evidenceIdColumn;

    @FXML
    private TableColumn<EvidenceRow, String> evidenceStatusColumn;

    @FXML
    private TableColumn<EvidenceRow, String> evidenceCustodianColumn;

    @FXML
    private ProgressBar readinessBar;

    @FXML
    private ProgressBar integrityBar;

    @FXML
    private ProgressBar signalBar;

    private final SecurityController securityController = new SecurityController();
    private final EvidenceController evidenceController = new EvidenceController();
    private final CaseController caseController = new CaseController();

    @FXML
    private void initialize() {
        configureEvidenceTable();
        seedActivityFeed();
        seedMetrics();
        refreshStatus();
        showSecurityPanel();
    }

    @FXML
    private void showSecurityPanel() {
        workspaceTitleLabel.setText("Security Operations");
        activityList.getItems().add(0, "Security sweep executed at " + timeLabel.getText());
    }

    @FXML
    private void showEvidencePanel() {
        workspaceTitleLabel.setText("Evidence Operations");
        activityList.getItems().add(0, "Evidence scan refreshed at " + timeLabel.getText());
    }

    @FXML
    private void showCasePanel() {
        workspaceTitleLabel.setText("Case Operations");
        activityList.getItems().add(0, "Case dashboard synced at " + timeLabel.getText());
    }

    @FXML
    private void refreshStatus() {
        boolean lockdownActive = SystemState.getInstance().isLockdownActive();
        String status = lockdownActive ? "Lockdown: ACTIVE" : "Lockdown: INACTIVE";
        lockdownStatusLabel.setText(status);
        threatLabel.setText(lockdownActive ? "Threat: Critical" : "Threat: Low");
    }

    private void configureEvidenceTable() {
        evidenceIdColumn.setCellValueFactory(data -> data.getValue().evidenceIdProperty());
        evidenceStatusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        evidenceCustodianColumn.setCellValueFactory(data -> data.getValue().custodianProperty());
        ObservableList<EvidenceRow> rows = FXCollections.observableArrayList(
            new EvidenceRow("EVD-001", "In Transit", "Agent Vega"),
            new EvidenceRow("EVD-118", "Secured", "Agent Nova"),
            new EvidenceRow("EVD-404", "Processing", "Agent Orion")
        );
        evidenceTable.setItems(rows);
    }

    private void seedActivityFeed() {
        activityList.setItems(FXCollections.observableArrayList(
            "Neon relay online",
            "Security stack ready: " + securityController.getClass().getSimpleName(),
            "Evidence stack ready: " + evidenceController.getClass().getSimpleName(),
            "Case stack ready: " + caseController.getClass().getSimpleName(),
            "Audit trail synchronized",
            "Evidence queue stabilized",
            "Case factory ready"
        ));
    }

    private void seedMetrics() {
        casesCountLabel.setText("18");
        evidenceCountLabel.setText("126");
        agentsCountLabel.setText("9");
        alertsCountLabel.setText("3");
        readinessBar.setProgress(0.86);
        integrityBar.setProgress(0.92);
        signalBar.setProgress(0.78);
        timeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    private static class EvidenceRow {
        private final SimpleStringProperty evidenceId;
        private final SimpleStringProperty status;
        private final SimpleStringProperty custodian;

        private EvidenceRow(String evidenceId, String status, String custodian) {
            this.evidenceId = new SimpleStringProperty(evidenceId);
            this.status = new SimpleStringProperty(status);
            this.custodian = new SimpleStringProperty(custodian);
        }

        private SimpleStringProperty evidenceIdProperty() {
            return evidenceId;
        }

        private SimpleStringProperty statusProperty() {
            return status;
        }

        private SimpleStringProperty custodianProperty() {
            return custodian;
        }
    }
}
