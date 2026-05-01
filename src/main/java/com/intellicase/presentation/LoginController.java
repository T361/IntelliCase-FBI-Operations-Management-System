package com.intellicase.presentation;

import com.intellicase.dao.AppUserDao;
import com.intellicase.domain.AppUser;

import javafx.fxml.FXML;
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

    private final AppUserDao userDao = new AppUserDao();
    private boolean showingPassword = false;

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

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        AppUser user = userDao.authenticate(username, password);
        if (user == null) {
            showError("Invalid credentials. Access denied.");
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
