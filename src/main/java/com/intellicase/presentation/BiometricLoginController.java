package com.intellicase.presentation;

import java.awt.image.BufferedImage;

import com.intellicase.application.FaceRecognitionService;
import com.intellicase.application.QrCodeService;
import com.intellicase.application.WebcamService;
import com.intellicase.dao.BiometricDao;
import com.intellicase.domain.AppUser;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

/**
 * Controller for BiometricLogin.fxml.
 *
 * Handles two authentication modes:
 *  • FACE  — live webcam feed; matches against stored face templates.
 *  • QR    — live webcam feed; decodes a QR code held up to the camera.
 *
 * GRASP Controller: coordinates WebcamService, FaceRecognitionService and
 * QrCodeService without containing business logic itself.
 */
public class BiometricLoginController {

    /** Mode shared between the calling controller and this one. */
    public enum Mode {
        FACE, QR
    }

    /** Set by the calling controller before navigating to this screen. */
    private static Mode pendingMode = Mode.FACE;

    public static void setPendingMode(Mode m) {
        pendingMode = m;
    }

    // ── FXML nodes ──────────────────────────────────────────────────────────

    @FXML private ImageView        cameraView;
    @FXML private Label            statusLabel;
    @FXML private Label            modeLabel;
    @FXML private Label            headingLabel;
    @FXML private Label            instructionLabel;
    @FXML private VBox             scanningIndicatorBox;
    @FXML private ProgressIndicator spinner;

    // ── Services ─────────────────────────────────────────────────────────────

    private final WebcamService         webcamService = WebcamService.getInstance();
    private final BiometricDao          biometricDao  = new BiometricDao();
    private final FaceRecognitionService faceService
        = new FaceRecognitionService(biometricDao);
    private final QrCodeService          qrService    = QrCodeService.getInstance();

    // ── State ────────────────────────────────────────────────────────────────

    private Mode   mode;
    private int    matchAttempts   = 0;
    private int    frameCount      = 0;
    /** Process every Nth frame for recognition (reduces CPU load). */
    private static final int PROCESS_EVERY = 5;
    private volatile boolean authenticated = false;

    // ─────────────────────────────────────────────────────────────────────────
    // FXML lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void initialize() {
        this.mode = pendingMode;
        configureUiForMode();
        startWebcam();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI configuration
    // ─────────────────────────────────────────────────────────────────────────

    private void configureUiForMode() {
        if (mode == Mode.FACE) {
            String title = "BIOMETRIC FACE RECOGNITION";
            modeLabel.setText(title);
            headingLabel.setText(title);
            instructionLabel.setText(
                "Look directly at the camera.\nThe system will identify you automatically.");
        } else {
            String title = "QR CODE SCANNER";
            modeLabel.setText(title);
            headingLabel.setText(title);
            instructionLabel.setText(
                "Hold your QR login code up to the camera.\n"
                + "Generate your code from your profile after first credential login.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Webcam streaming
    // ─────────────────────────────────────────────────────────────────────────

    private void startWebcam() {
        setStatus("Initialising camera…", false);

        webcamService.start(
            // background thread — run recognition
            this::processRawFrame,
            // JavaFX thread — update preview image
            this::updatePreview
        );
    }

    /**
     * Called on the background thread with every raw frame.
     * Only processes every PROCESS_EVERY-th frame.
     */
    private void processRawFrame(BufferedImage frame) {
        if (authenticated) {
            return;
        }
        frameCount++;
        if (frameCount % PROCESS_EVERY != 0) {
            return;
        }

        if (mode == Mode.FACE) {
            processFaceFrame(frame);
        } else {
            processQrFrame(frame);
        }
    }

    private void processFaceFrame(BufferedImage frame) {
        AppUser user = faceService.recognise(frame);
        if (user != null) {
            authenticated = true;
            javafx.application.Platform.runLater(() -> onAuthSuccess(user));
        } else {
            matchAttempts++;
            if (matchAttempts % 15 == 0) {
                String msg = matchAttempts < 60
                    ? "Scanning… ensure good lighting and face the camera."
                    : "No match found. Use credentials or enrol your face first.";
                javafx.application.Platform.runLater(() -> setStatus(msg, false));
            }
        }
    }

    private void processQrFrame(BufferedImage frame) {
        String token = qrService.decodeFrame(frame);
        if (token == null) {
            return;
        }

        AppUser user = qrService.validateToken(token);
        if (user != null) {
            authenticated = true;
            javafx.application.Platform.runLater(() -> onAuthSuccess(user));
        } else {
            javafx.application.Platform.runLater(()
                -> setStatus("⚠ Invalid or expired QR code. Regenerate from your profile.", true));
        }
    }

    /** Update the camera preview ImageView on the JavaFX thread. */
    private void updatePreview(BufferedImage frame) {
        if (frame == null) {
            return;
        }
        Image fx = SwingFXUtils.toFXImage(frame, null);
        cameraView.setImage(fx);
        if (spinner.isVisible()) {
            spinner.setVisible(false);
            setStatus(mode == Mode.FACE ? "Face scanning active…" : "Point QR code at camera…", false);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Authentication result
    // ─────────────────────────────────────────────────────────────────────────

    private void onAuthSuccess(AppUser user) {
        webcamService.stop();
        setStatus("✔ Identity confirmed — " + user.getFullName(), false);
        AudioFeedbackManager.playClick();
        SessionManager.getInstance().login(user);

        // Short pause for visual feedback, then navigate
        javafx.animation.PauseTransition pause =
            new javafx.animation.PauseTransition(javafx.util.Duration.millis(900));
        pause.setOnFinished(e -> {
            if ("PUBLIC_USER".equals(user.getRole())) {
                AppStageManager.showPublicPortal();
            } else {
                AppStageManager.showMainApp();
            }
        });
        pause.play();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Button handlers
    // ─────────────────────────────────────────────────────────────────────────

    @FXML
    private void handleCancel() {
        webcamService.stop();
        PreAuthRouter.getInstance().navigateTo("/ui/LoginPage.fxml");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void setStatus(String msg, boolean error) {
        statusLabel.setText(msg);
        statusLabel.setStyle(error
            ? "-fx-text-fill: #ff4444; -fx-font-size: 12px;"
            : "-fx-text-fill: #00ff88; -fx-font-size: 12px;");
    }
}
