package com.intellicase.presentation;

import com.intellicase.dao.AppUserDao;
import com.intellicase.domain.AppUser;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for LoginPage.fxml.
 * Validates credentials, stores the session, then routes to the
 * correct portal based on role (PUBLIC_USER vs internal staff).
 */
public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisible;

    @FXML
    private Label errorLabel;

    @FXML
    private ComboBox<String> roleComboBox;

    /** Maps human-readable display label → internal role string. */
    private static final Map<String, String> ROLE_MAP = new LinkedHashMap<>();
    static {
        ROLE_MAP.put("FBI Director",            "FBI_DIRECTOR");
        ROLE_MAP.put("Case Supervisor",         "CASE_SUPERVISOR");
        ROLE_MAP.put("Field Agent",             "FIELD_AGENT");
        ROLE_MAP.put("Intelligence Analyst",    "INTELLIGENCE_ANALYST");
        ROLE_MAP.put("Forensic Specialist",     "FORENSIC_SPECIALIST");
        ROLE_MAP.put("Public User",             "PUBLIC_USER");
    }

    private final AppUserDao userDao = new AppUserDao();
    private boolean showingPassword = false;

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll(ROLE_MAP.keySet());
    }

    @FXML
    private void togglePassword() {
        if (showingPassword) {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
        } else {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
        }
        showingPassword = !showingPassword;
    }

    private String getPassword() {
        return showingPassword ? passwordVisible.getText() : passwordField.getText();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = getPassword();
        String selectedLabel = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || selectedLabel == null) {
            showError("Please enter credentials and select your role.");
            return;
        }

        String selectedRole = ROLE_MAP.getOrDefault(selectedLabel, "");

        AppUser user = userDao.authenticate(username, password);
        if (user == null) {
            showError("Invalid credentials. Access denied.");
            AudioFeedbackManager.playError();
            return;
        }

        // Enforce role-based access: user must hold the selected clearance
        if (!selectedRole.equals(user.getRole())) {
            showError("Access denied: Your account does not hold the '"
                + selectedLabel + "' clearance level.");
            AudioFeedbackManager.playError();
            return;
        }

        SessionManager.getInstance().login(user);
        AudioFeedbackManager.playClick();

        if ("PUBLIC_USER".equals(user.getRole())) {
            AppStageManager.showPublicPortal();
        } else {
            AppStageManager.showMainApp();
        }
    }

    @FXML
    private void handleFaceLogin() {
        AudioFeedbackManager.playClick();
        BiometricLoginController.setPendingMode(BiometricLoginController.Mode.FACE);
        PreAuthRouter.getInstance().navigateTo("/ui/BiometricLogin.fxml");
    }

    @FXML
    private void handleQrLogin() {
        AudioFeedbackManager.playClick();
        BiometricLoginController.setPendingMode(BiometricLoginController.Mode.QR);
        PreAuthRouter.getInstance().navigateTo("/ui/BiometricLogin.fxml");
    }

    @FXML
    private void goBack() {
        PreAuthRouter.getInstance().navigateTo("/ui/LandingPage.fxml");
    }

    @FXML
    private void goSignup() {
        PreAuthRouter.getInstance().navigateTo("/ui/SignupPage.fxml");
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }
}
