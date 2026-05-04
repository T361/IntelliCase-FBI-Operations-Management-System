package com.intellicase.presentation;

import com.intellicase.application.QrCodeService;
import com.intellicase.domain.AppUser;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for QrDisplay.fxml.
 * Generates a one-time QR login token for the current authenticated user
 * and displays it on screen. The token is valid for 5 minutes.
 *
 * GRASP Controller: thin; delegates all token logic to QrCodeService.
 */
public class QrDisplayController {

    private static final int QR_SIZE = 300;

    @FXML private ImageView qrImageView;
    @FXML private Label     userLabel;
    @FXML private Label     expiryLabel;
    @FXML private Label     instructionLabel;
    @FXML private Label     statusLabel;

    private final QrCodeService qrService = QrCodeService.getInstance();

    @FXML
    private void initialize() {
        AppUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            setStatus("⚠ No active session.", true);
            return;
        }
        userLabel.setText(user.getFullName() + "  [" + user.getRole().replace('_', ' ') + "]");
        generateQr(user);
    }

    private void generateQr(AppUser user) {
        Image qr = qrService.generateLoginQr(user.getId(), QR_SIZE);
        if (qr == null) {
            setStatus("⚠ Failed to generate QR code. Check database connection.", true);
            return;
        }
        qrImageView.setImage(qr);
        long expiresInMin = 5;
        expiryLabel.setText("⏱ Valid for " + expiresInMin + " minutes from generation.");
        instructionLabel.setText(
            "On the login screen select QR SCAN, then hold this code up to the camera.");
        setStatus("QR code generated successfully. Use it within 5 minutes.", false);
    }

    /** Re-generate a fresh QR token (old token is invalidated). */
    @FXML
    private void handleRefresh() {
        AppUser user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            generateQr(user);
            AudioFeedbackManager.playClick();
        }
    }

    @FXML
    private void handleClose() {
        ViewRouter.getInstance().navigateTo("/ui/MainMenu.fxml");
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error
            ? "-fx-text-fill: #ff4444; -fx-font-size: 12px;"
            : "-fx-text-fill: #00ff88; -fx-font-size: 12px;");
    }
}
