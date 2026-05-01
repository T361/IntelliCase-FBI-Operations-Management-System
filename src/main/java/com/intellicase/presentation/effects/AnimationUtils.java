package com.intellicase.presentation.effects;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Utility class for UI animations like floating and breathing effects.
 */
public class AnimationUtils {
    public static void applyFloating(Node node) {
        TranslateTransition floating = new TranslateTransition(Duration.seconds(3), node);
        floating.setByY(-10);
        floating.setCycleCount(Animation.INDEFINITE);
        floating.setAutoReverse(true);
        floating.setInterpolator(Interpolator.EASE_BOTH);
        floating.play();
    }

    public static void applyBreathing(Node node) {
        ScaleTransition breathing = new ScaleTransition(Duration.seconds(2), node);
        breathing.setByX(0.05);
        breathing.setByY(0.05);
        breathing.setCycleCount(Animation.INDEFINITE);
        breathing.setAutoReverse(true);
        breathing.setInterpolator(Interpolator.EASE_BOTH);
        breathing.play();
    }
}
