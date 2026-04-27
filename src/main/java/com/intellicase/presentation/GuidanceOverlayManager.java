package com.intellicase.presentation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Guidance overlay system that pulses DropShadow effects and attaches directional
 * tooltips to UI nodes for step-by-step user onboarding.
 */
public final class GuidanceOverlayManager {
    private static Timeline currentPulse;

    private GuidanceOverlayManager() {
    }

    public static void highlightNode(Node node, String message) {
        clearHighlights();
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(255, 255, 0, 0.9));
        glow.setRadius(20);
        glow.setSpread(0.4);
        node.setEffect(glow);

        currentPulse = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(glow.radiusProperty(), 10)),
            new KeyFrame(Duration.millis(600), new KeyValue(glow.radiusProperty(), 25)),
            new KeyFrame(Duration.millis(1200), new KeyValue(glow.radiusProperty(), 10))
        );
        currentPulse.setCycleCount(Timeline.INDEFINITE);
        currentPulse.play();

        Tooltip tip = new Tooltip(message);
        tip.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-text-fill: #00f3ff; "
            + "-fx-font-size: 13px; -fx-border-color: #00f3ff; -fx-border-width: 1; "
            + "-fx-padding: 8 12; -fx-font-weight: bold;");
        tip.setShowDelay(Duration.ZERO);
        Tooltip.install(node, tip);
    }

    public static void highlightSequence(Node[] nodes, String[] messages, Runnable onComplete) {
        highlightStep(nodes, messages, 0, onComplete);
    }

    private static void highlightStep(Node[] nodes, String[] messages, int index, Runnable onComplete) {
        if (index >= nodes.length) {
            clearHighlights();
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        highlightNode(nodes[index], messages[index]);
        nodes[index].setOnMouseClicked(e -> {
            clearHighlights();
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(ev -> highlightStep(nodes, messages, index + 1, onComplete));
            pause.play();
        });
    }

    public static void clearHighlights() {
        if (currentPulse != null) {
            currentPulse.stop();
            currentPulse = null;
        }
    }
}
