package com.intellicase.presentation;

import java.util.List;
import java.util.stream.Collectors;

import com.intellicase.dao.CaseFileDao;
import com.intellicase.domain.CaseFile;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for PublicPortal.fxml.
 * Beautiful animated public portal with:
 * - Staggered card fade-in animations
 * - Scale/glow hover effects
 * - Click-to-detail overlay (slide-in from below)
 * - Auto-refresh every 15s from DB
 * - Search, filter, and tip submission
 */
public class PublicPortalController {

    @FXML
    private StackPane rootPane;

    @FXML
    private Label userGreetingLabel;

    @FXML
    private Label liveLabel;

    @FXML
    private Label caseCountLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> priorityFilter;

    @FXML
    private FlowPane caseCardsPane;

    @FXML
    private ScrollPane mainScroll;

    // Detail overlay fields
    @FXML
    private StackPane detailOverlay;

    @FXML
    private VBox detailPanel;

    @FXML
    private Label detailCaseId;

    @FXML
    private Label detailStatus;

    @FXML
    private Label detailPriority;

    @FXML
    private Label detailLocation;

    @FXML
    private Label detailDescription;

    // Tip fields
    @FXML
    private TextField tipCaseIdField;

    @FXML
    private TextField tipEmailField;

    @FXML
    private TextArea tipDetailsArea;

    @FXML
    private Label tipStatusLabel;

    private final CaseFileDao caseDao = new CaseFileDao();
    private List<CaseFile> allPublicCases;
    private CaseFile selectedCase;
    private Timeline autoRefreshTimer;
    private Timeline liveBlinkTimer;

    @FXML
    private void initialize() {
        SessionManager sm = SessionManager.getInstance();
        if (sm.isLoggedIn()) {
            userGreetingLabel.setText(sm.getDisplayName() + "  ·  CIVILIAN");
        }

        statusFilter.getItems().addAll("ALL STATUS", "OPEN", "ACTIVE", "CLOSED", "COLD");
        statusFilter.setValue("ALL STATUS");
        priorityFilter.getItems().addAll("ALL PRIORITY", "LOW", "MEDIUM", "HIGH", "CRITICAL");
        priorityFilter.setValue("ALL PRIORITY");

        startLiveBlink();
        loadPublicCases();
        startAutoRefresh();
    }

    // ── Auto-refresh ─────────────────────────────────────────────────────────

    private void startAutoRefresh() {
        autoRefreshTimer = new Timeline(
            new KeyFrame(Duration.seconds(15), e -> Platform.runLater(this::silentRefresh)));
        autoRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimer.play();
    }

    private void silentRefresh() {
        List<CaseFile> fresh = caseDao.findAll().stream()
            .filter(c -> !"CLASSIFIED".equalsIgnoreCase(c.getStatus()))
            .collect(Collectors.toList());

        if (fresh.size() != allPublicCases.size()) {
            allPublicCases = fresh;
            renderCases(fresh, true);
        }
    }

    @FXML
    private void handleManualRefresh() {
        loadPublicCases();
        pulseLabel(caseCountLabel);
    }

    private void startLiveBlink() {
        liveBlinkTimer = new Timeline(
            new KeyFrame(Duration.seconds(1.2), e -> {
                liveLabel.setOpacity(liveLabel.getOpacity() < 0.5 ? 1.0 : 0.3);
            }));
        liveBlinkTimer.setCycleCount(Timeline.INDEFINITE);
        liveBlinkTimer.play();
    }

    // ── Data loading ─────────────────────────────────────────────────────────

    private void loadPublicCases() {
        allPublicCases = caseDao.findAll().stream()
            .filter(c -> !"CLASSIFIED".equalsIgnoreCase(c.getStatus()))
            .collect(Collectors.toList());
        caseCountLabel.setText(allPublicCases.size() + " cases");
        renderCases(allPublicCases, true);
    }

    private void renderCases(List<CaseFile> cases, boolean animate) {
        caseCardsPane.getChildren().clear();

        if (cases.isEmpty()) {
            Label empty = new Label("No public cases match your search criteria.");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-size: 14px;"
                + " -fx-padding: 20 0;");
            caseCardsPane.getChildren().add(empty);
            return;
        }

        for (int i = 0; i < cases.size(); i++) {
            VBox card = buildCaseCard(cases.get(i));
            caseCardsPane.getChildren().add(card);

            if (animate) {
                card.setOpacity(0);
                card.setTranslateY(20);
                int delay = i * 70;
                Timeline t = new Timeline(new KeyFrame(Duration.millis(delay), e -> {
                    FadeTransition fade = new FadeTransition(Duration.millis(350), card);
                    fade.setToValue(1.0);
                    TranslateTransition slide =
                        new TranslateTransition(Duration.millis(350), card);
                    slide.setToY(0);
                    fade.play();
                    slide.play();
                }));
                t.play();
            }
        }
    }

    // ── Card builder ─────────────────────────────────────────────────────────

    private VBox buildCaseCard(CaseFile cf) {
        VBox card = new VBox(12);
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.setStyle(baseCardStyle());
        card.setPadding(new javafx.geometry.Insets(18, 20, 18, 20));

        // Status colour
        String statusColor = statusColor(cf.getStatus());

        // Header row
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label caseIdLbl = new Label(cf.getCaseId());
        caseIdLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #C9A94D;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusChip = new Label(cf.getStatus());
        statusChip.setStyle("-fx-background-color: " + statusColor + "22;"
            + "-fx-text-fill: " + statusColor + "; -fx-padding: 3 12;"
            + "-fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;");

        headerRow.getChildren().addAll(caseIdLbl, spacer, statusChip);

        // Description
        Label descLbl = new Label(cf.getDescription());
        descLbl.setWrapText(true);
        descLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.70);");

        // Footer row
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label locLbl = new Label("📍 " + cf.getLocation());
        locLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.38);");

        Region fSpacer = new Region();
        HBox.setHgrow(fSpacer, Priority.ALWAYS);

        Label priLbl = new Label(cf.getPriority());
        String priColor = priorityColor(cf.getPriority());
        priLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + priColor + ";"
            + "-fx-font-weight: bold;");

        Label clickHint = new Label("VIEW DETAILS →");
        clickHint.setStyle("-fx-font-size: 10px; -fx-text-fill: #1F6FEB;");

        footer.getChildren().addAll(locLbl, fSpacer, priLbl);
        card.getChildren().addAll(headerRow, descLbl, footer, clickHint);

        // Hover effects
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(160), card);
        scaleUp.setToX(1.03);
        scaleUp.setToY(1.03);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(160), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);

        card.setOnMouseEntered(e -> {
            card.setStyle(hoverCardStyle(statusColor));
            scaleDown.stop();
            scaleUp.playFromStart();
        });
        card.setOnMouseExited(e -> {
            card.setStyle(baseCardStyle());
            scaleUp.stop();
            scaleDown.playFromStart();
        });
        card.setOnMousePressed(e -> card.setScaleX(0.98));
        card.setOnMouseReleased(e -> card.setScaleX(1.03));
        card.setOnMouseClicked(e -> showCaseDetail(cf));
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");

        return card;
    }

    private String baseCardStyle() {
        return "-fx-background-color: rgba(13,36,68,0.85);"
            + "-fx-border-color: rgba(201,169,77,0.18); -fx-border-width: 1.2;"
            + "-fx-border-radius: 12; -fx-background-radius: 12;"
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.2, 0, 2);"
            + "-fx-cursor: hand;";
    }

    private String hoverCardStyle(String accentColor) {
        return "-fx-background-color: rgba(13,36,68,0.97);"
            + "-fx-border-color: " + accentColor + "; -fx-border-width: 1.5;"
            + "-fx-border-radius: 12; -fx-background-radius: 12;"
            + "-fx-effect: dropshadow(gaussian, " + accentColor + ", 20, 0.25, 0, 0);"
            + "-fx-cursor: hand;";
    }

    private String statusColor(String status) {
        if (status == null) {
            return "#888";
        }
        switch (status.toUpperCase()) {
            case "OPEN": return "#1F6FEB";
            case "ACTIVE": return "#C9A94D";
            case "CLOSED": return "#00ff88";
            default: return "#888";
        }
    }

    private String priorityColor(String priority) {
        if (priority == null) {
            return "#888";
        }
        switch (priority.toUpperCase()) {
            case "CRITICAL": return "#ff4444";
            case "HIGH": return "#ff8800";
            case "MEDIUM": return "#C9A94D";
            default: return "#888";
        }
    }

    // ── Detail overlay ────────────────────────────────────────────────────────

    private void showCaseDetail(CaseFile cf) {
        this.selectedCase = cf;

        detailCaseId.setText(cf.getCaseId());
        detailStatus.setText("STATUS: " + cf.getStatus());
        detailStatus.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;"
            + "-fx-text-fill: " + statusColor(cf.getStatus()) + ";");
        detailPriority.setText(cf.getPriority());
        detailPriority.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;"
            + "-fx-text-fill: " + priorityColor(cf.getPriority()) + ";");
        detailLocation.setText("📍 " + cf.getLocation());
        detailDescription.setText(cf.getDescription());

        detailOverlay.setVisible(true);
        detailOverlay.setManaged(true);
        detailOverlay.setOpacity(0);

        // Slide panel up from below
        detailPanel.setTranslateY(60);

        FadeTransition fade = new FadeTransition(Duration.millis(220), detailOverlay);
        fade.setToValue(1.0);

        TranslateTransition slide = new TranslateTransition(Duration.millis(280), detailPanel);
        slide.setToY(0);
        slide.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        fade.play();
        slide.play();

        AudioFeedbackManager.playClick();
    }

    @FXML
    private void closeDetail() {
        FadeTransition fade = new FadeTransition(Duration.millis(180), detailOverlay);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            detailOverlay.setVisible(false);
            detailOverlay.setManaged(false);
            selectedCase = null;
        });
        fade.play();
    }

    @FXML
    private void submitTipForSelected() {
        if (selectedCase != null) {
            tipCaseIdField.setText(selectedCase.getCaseId());
        }
        closeDetail();
        // Scroll to tip section
        mainScroll.setVvalue(1.0);
    }

    // ── Search & Filter ───────────────────────────────────────────────────────

    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        String status = statusFilter.getValue();
        String priority = priorityFilter.getValue();

        List<CaseFile> filtered = allPublicCases.stream()
            .filter(c -> {
                boolean matchKw = kw.isEmpty()
                    || c.getCaseId().toLowerCase().contains(kw)
                    || c.getDescription().toLowerCase().contains(kw)
                    || c.getLocation().toLowerCase().contains(kw);
                boolean matchStatus = "ALL STATUS".equals(status)
                    || c.getStatus().equalsIgnoreCase(status);
                boolean matchPriority = "ALL PRIORITY".equals(priority)
                    || c.getPriority().equalsIgnoreCase(priority);
                return matchKw && matchStatus && matchPriority;
            })
            .collect(Collectors.toList());

        caseCountLabel.setText(filtered.size() + " of " + allPublicCases.size() + " cases");
        renderCases(filtered, true);
    }

    @FXML
    private void handleClear() {
        searchField.clear();
        statusFilter.setValue("ALL STATUS");
        priorityFilter.setValue("ALL PRIORITY");
        caseCountLabel.setText(allPublicCases.size() + " cases");
        renderCases(allPublicCases, true);
    }

    // ── Tip submission ────────────────────────────────────────────────────────

    @FXML
    private void handleSubmitTip() {
        String details = tipDetailsArea.getText().trim();
        String caseId = tipCaseIdField.getText().trim();
        String email = tipEmailField.getText().trim();

        if (details.isEmpty()) {
            tipStatusLabel.setText("Please describe your tip before submitting.");
            tipStatusLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 12px;");
            return;
        }

        // If no case ID provided, use a default unassigned pool ID
        if (caseId.isEmpty()) {
            caseId = "UNASSIGNED";
        }

        String tipId = "TIP-" + System.currentTimeMillis();
        String hash = Integer.toHexString(details.hashCode());
        
        // Save as Evidence so Field Agents can see it
        com.intellicase.dao.EvidenceDao evidenceDao = new com.intellicase.dao.EvidenceDao();
        com.intellicase.domain.Evidence tipEvidence = new com.intellicase.domain.Evidence(
            tipId, 
            caseId, 
            "UNVERIFIED", // status
            email.isEmpty() ? "ANONYMOUS" : email, // custodian
            hash, 
            1 // sensitivity level (1 = Public/Low)
        );
        evidenceDao.create(tipEvidence);

        // Log the tip submission
        com.intellicase.dao.AuditLogDao auditDao = new com.intellicase.dao.AuditLogDao();
        auditDao.create(new com.intellicase.domain.AuditLogEntry("PUBLIC_TIP_SUBMITTED", tipId, "PUBLIC_PORTAL"));

        tipStatusLabel.setText(
            "\u2713 Tip submitted. Thank you for assisting the investigation. Reference ID: " + tipId);
        tipStatusLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 12px;");
        tipCaseIdField.clear();
        tipEmailField.clear();
        tipDetailsArea.clear();
        AudioFeedbackManager.playClick();
        pulseLabel(tipStatusLabel);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        if (autoRefreshTimer != null) {
            autoRefreshTimer.stop();
        }
        if (liveBlinkTimer != null) {
            liveBlinkTimer.stop();
        }
        SessionManager.getInstance().logout();
        AppStageManager.showLanding();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void pulseLabel(Label label) {
        ScaleTransition s = new ScaleTransition(Duration.millis(120), label);
        s.setByX(0.08);
        s.setByY(0.08);
        s.setCycleCount(2);
        s.setAutoReverse(true);
        s.play();
    }
}
