package com.intellicase.presentation.effects;

import com.intellicase.presentation.GuidanceOverlayManager;
import javafx.scene.Node;

/**
 * Wrapper for GuidanceOverlayManager to maintain compatibility with RootLayoutController.
 */
public class HUDGuidanceOverlay {
    public static void highlight(Node node, String message) {
        GuidanceOverlayManager.highlightNode(node, message);
    }

    public static void clear() {
        GuidanceOverlayManager.clearHighlights();
    }
}
