package com.intellicase.presentation;

import java.awt.image.BufferedImage;

import com.intellicase.application.FaceRecognitionService;
import com.intellicase.application.WebcamService;
import com.intellicase.dao.BiometricDao;
import com.intellicase.domain.AppUser;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for FaceEnrol.fxml.
 * Allows an already-authenticated user to register their face so
 * that future logins can use the Face ID biometric method.
 *
 * GRASP Controller: coordinates WebcamService and FaceRecognitionService.
 */
public class FaceEnrolController {

    @FXML private ImageView        cameraView;
    @FXML private Label            statusLabel;
    @FXML private Label            enrolledLabel;
    @FXML private Button           captureButton;
    @FXML private ProgressIndicator spinner;

    private final BiometricDao           biometricDao = new BiometricDao();
    private final FaceRecognitionService  faceService  = new FaceRecognitionService(biometricDao);
    private final WebcamService          webcamService = WebcamService.getInstance();

    private volatile BufferedImage latestFrame = null;

    @FXML
    private void initialize() {
        AppUser user = SessionManager.getInstance().getCurrentUser();
        boolean alreadyEnrolled = user != null && biometricDao.hasFaceTemplate(user.getId());
        if (alreadyEnrolled) {
            enrolledLabel.setText("\u2714 Face template already registered. You can update it.");
            enrolledLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 12px;");
        } else {
            enrolledLabel.setText("No face template registered yet.");
            enrolledLabel.setStyle("-fx-text-fill: rgba(212,175,55,0.6); -fx-font-size: 12px;");
        }
        startCamera();
    }

    private void startCamera() {
        setStatus("Starting camera…", false);
        webcamService.start(
            frame -> latestFrame = frame,
            frame -> {
                Image fx = SwingFXUtils.toFXImage(frame, null);
                cameraView.setImage(fx);
                if (spinner.isVisible()) {
                    spinner.setVisible(false);
                    setStatus("Position your face in the frame, then click CAPTURE.", false);
                }
            }
        );
    }

    @FXML
    private void handleCapture() {
        AppUser user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            setStatus("⚠ No active session. Please log in first.", true);
            return;
        }
        BufferedImage frame = latestFrame;
        if (frame == null) {
            setStatus("⚠ Camera not ready. Wait for preview to appear.", true);
            return;
        }
        captureButton.setDisable(true);
        setStatus("Processing face template…", false);
        String descriptor = faceService.enrol(user.getId(), frame);
        if (descriptor != null) {
            setStatus("✔ Face template saved! You can now use Face ID to log in.", false);
            enrolledLabel.setText("✔ Face template registered successfully.");
            enrolledLabel.setStyle("-fx-text-fill: #00ff88; -fx-font-size: 12px;");
            AudioFeedbackManager.playClick();
        } else {
            setStatus("⚠ Enrolment failed. Ensure the camera is working and try again.", true);
        }
        captureButton.setDisable(false);
    }

    @FXML
    private void handleClose() {
        webcamService.stop();
        ViewRouter.getInstance().navigateTo("/ui/MainMenu.fxml");
    }

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error
            ? "-fx-text-fill: #ff4444; -fx-font-size: 12px;"
            : "-fx-text-fill: #00ff88; -fx-font-size: 12px;");
    }
}
