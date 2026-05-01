package com.intellicase.presentation;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for LandingPage.fxml.
 * Applies entry animation on the hero title and programmatic
 * scale hover effects on all four feature cards.
 */
public class LandingPageController {
    @FXML
    private Label heroTitle;

    @FXML
    private VBox card1;

    @FXML
    private VBox card2;

    @FXML
    private VBox card3;

    @FXML
    private VBox card4;

    @FXML
    private void initialize() {
        applyHeroAnimation();
        applyCardHovers(card1);
        applyCardHovers(card2);
        applyCardHovers(card3);
        applyCardHovers(card4);
    }

    private void applyHeroAnimation() {
        heroTitle.setOpacity(0);
        heroTitle.setScaleX(0.8);
        heroTitle.setScaleY(0.8);

        javafx.animation.FadeTransition fade =
            new javafx.animation.FadeTransition(Duration.millis(900), heroTitle);
        fade.setToValue(1.0);

        ScaleTransition scale =
            new ScaleTransition(Duration.millis(900), heroTitle);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.setInterpolator(Interpolator.EASE_OUT);

        fade.play();
        scale.play();
    }

    private void applyCardHovers(VBox card) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(160), card);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(160), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition pressDown = new ScaleTransition(Duration.millis(80), card);
        pressDown.setToX(0.97);
        pressDown.setToY(0.97);

        ScaleTransition pressUp = new ScaleTransition(Duration.millis(80), card);
        pressUp.setToX(1.05);
        pressUp.setToY(1.05);

        card.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.playFromStart();
        });
        card.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.playFromStart();
        });
        card.setOnMousePressed(e -> pressDown.playFromStart());
        card.setOnMouseReleased(e -> pressUp.playFromStart());
    }

    @FXML
    private void goLogin() {
        PreAuthRouter.getInstance().navigateTo("/ui/LoginPage.fxml");
    }

    @FXML
    private void goSignup() {
        PreAuthRouter.getInstance().navigateTo("/ui/SignupPage.fxml");
    }
}
