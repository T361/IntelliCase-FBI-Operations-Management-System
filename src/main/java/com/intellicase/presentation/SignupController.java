package com.intellicase.presentation;

import com.intellicase.dao.AppUserDao;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for SignupPage.fxml.
 * Validates registration fields, creates the user, includes password toggle.
 */
public class SignupController {
    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordVisible;

    @FXML
    private PasswordField confirmField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private Label statusLabel;

    private final AppUserDao userDao = new AppUserDao();
    private boolean showingPassword = false;

    @FXML
    private void initialize() {
        roleCombo.getItems().addAll(
            "PUBLIC_USER",
            "FIELD_AGENT",
            "CASE_SUPERVISOR",
            "FBI_DIRECTOR",
            "INTELLIGENCE_ANALYST",
            "FORENSIC_SPECIALIST"
        );
        roleCombo.setValue("PUBLIC_USER");
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
    private void handleSignup() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = getPassword();
        String confirm = confirmField.getText();
        String role = roleCombo.getValue();

        if (fullName.isEmpty() || username.isEmpty()
                || email.isEmpty() || password.isEmpty()) {
            showStatus("All fields are required.", false);
            return;
        }
        if (!password.equals(confirm)) {
            showStatus("Passwords do not match.", false);
            return;
        }
        if (password.length() < 8) {
            showStatus("Password must be at least 8 characters.", false);
            return;
        }

        if ("FBI_DIRECTOR".equals(role) && userDao.hasDirector()) {
            showStatus("An FBI Director already exists. Only one is permitted.", false);
            AudioFeedbackManager.playError();
            return;
        }

        boolean ok = userDao.register(username, fullName, email, password, role);
        if (ok) {
            showStatus("\u2713 Account created! You may now login.", true);
            AudioFeedbackManager.playClick();
        } else {
            showStatus("Username or email already in use.", false);
            AudioFeedbackManager.playError();
        }
    }

    @FXML
    private void goBack() {
        PreAuthRouter.getInstance().navigateTo("/ui/LandingPage.fxml");
    }

    @FXML
    private void goLogin() {
        PreAuthRouter.getInstance().navigateTo("/ui/LoginPage.fxml");
    }

    private void showStatus(String msg, boolean success) {
        statusLabel.setText(msg);
        statusLabel.setStyle(success
            ? "-fx-text-fill: #00ff88; -fx-font-size: 12px;"
            : "-fx-text-fill: #ff4444; -fx-font-size: 12px;");
    }
}
