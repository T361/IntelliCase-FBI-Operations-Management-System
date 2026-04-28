package com.intellicase.presentation;

import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * Crash-proof runtime audio synthesizer for global UI feedback.
 * Uses raw javax.sound.sampled byte-array generation to completely
 * bypass JavaFX media and OS-level PipeWire/PulseAudio dependencies.
 * All playback failures are silently swallowed (Exit 137 immunity).
 */
public final class AudioFeedbackManager {
    private static final int SAMPLE_RATE = 44100;
    private static final AudioFormat FORMAT =
        new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private static byte[] hoverTone;
    private static byte[] clickTone;
    private static byte[] errorTone;
    private static boolean initialized = false;

    private AudioFeedbackManager() {
    }

    /**
     * Lazy-initialize all synthesized tone buffers exactly once.
     */
    private static synchronized void init() {
        if (initialized) {
            return;
        }
        hoverTone = synthesizeSine(1200, 10, 0.25);
        clickTone = synthesizeClick(200, 1000, 30, 0.35);
        errorTone = synthesizeSine(160, 120, 0.40);
        initialized = true;
    }

    /**
     * Generate a pure sine wave tone as 16-bit signed PCM bytes.
     */
    private static byte[] synthesizeSine(int freqHz, int durationMs, double vol) {
        int numSamples = (SAMPLE_RATE * durationMs) / 1000;
        byte[] buf = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i * freqHz / SAMPLE_RATE;
            short sample = (short) (Math.sin(angle) * 32767.0 * vol);
            buf[i * 2] = (byte) (sample & 0xFF);
            buf[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return applyFade(buf);
    }

    /**
     * Generate a composite click: low-frequency square pulse followed
     * by a high-frequency sine ping, creating a punchy tactile feel.
     */
    private static byte[] synthesizeClick(int loHz, int hiHz,
                                          int durationMs, double vol) {
        int numSamples = (SAMPLE_RATE * durationMs) / 1000;
        int midpoint = numSamples / 3;
        byte[] buf = new byte[numSamples * 2];
        for (int i = 0; i < numSamples; i++) {
            double value;
            if (i < midpoint) {
                double angle = 2.0 * Math.PI * i * loHz / SAMPLE_RATE;
                value = Math.signum(Math.sin(angle)) * vol;
            } else {
                double angle = 2.0 * Math.PI * i * hiHz / SAMPLE_RATE;
                value = Math.sin(angle) * vol * 0.6;
            }
            short sample = (short) (value * 32767.0);
            buf[i * 2] = (byte) (sample & 0xFF);
            buf[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return applyFade(buf);
    }

    /**
     * Apply a short fade-in/fade-out envelope to eliminate click artifacts.
     */
    private static byte[] applyFade(byte[] buf) {
        int totalSamples = buf.length / 2;
        int fadeSamples = Math.min(totalSamples / 4, 200);
        for (int i = 0; i < fadeSamples; i++) {
            double ratio = (double) i / fadeSamples;
            scaleSample(buf, i, ratio);
            scaleSample(buf, totalSamples - 1 - i, ratio);
        }
        return buf;
    }

    /**
     * Scale a single 16-bit sample in a byte array by a given ratio.
     */
    private static void scaleSample(byte[] buf, int idx, double ratio) {
        int lo = buf[idx * 2] & 0xFF;
        int hi = buf[idx * 2 + 1];
        short sample = (short) ((hi << 8) | lo);
        sample = (short) (sample * ratio);
        buf[idx * 2] = (byte) (sample & 0xFF);
        buf[idx * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
    }

    /**
     * Play a raw PCM tone buffer on a daemon thread.
     * Auto-closes the Clip via LineListener after playback completes.
     */
    private static void playTone(byte[] toneData) {
        if (toneData == null) {
            return;
        }
        Thread t = new Thread(() -> {
            try {
                ByteArrayInputStream bais =
                    new ByteArrayInputStream(toneData);
                AudioInputStream ais =
                    new AudioInputStream(bais, FORMAT, toneData.length / 2);
                Clip clip = AudioSystem.getClip();
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.open(ais);
                clip.start();
            } catch (Exception | UnsatisfiedLinkError ex) {
                // Silent fail: audio is non-critical.
                // Linux PipeWire segfaults (Exit 137) are swallowed.
            }
        }, "audio-synth");
        t.setDaemon(true);
        t.start();
    }

    /** Play the hover blip tone. */
    public static void playHover() {
        init();
        playTone(hoverTone);
    }

    /** Play the click confirmation tone. */
    public static void playClick() {
        init();
        playTone(clickTone);
    }

    /** Play the error buzz tone. */
    public static void playError() {
        init();
        playTone(errorTone);
    }

    /** Attach hover and click listeners to a single Node. */
    public static void attachTo(Node node) {
        node.setOnMouseEntered(e -> playHover());
        node.setOnMouseClicked(e -> playClick());
    }

    /**
     * Recursively traverse the entire Scene graph and attach audio
     * feedback listeners to every interactive Node (Buttons, TextFields,
     * TextAreas). Called once from RootLayoutController after scene load.
     */
    public static void bindToScene(Scene scene) {
        if (scene == null || scene.getRoot() == null) {
            return;
        }
        init();
        bindRecursive(scene.getRoot());
    }

    private static void bindRecursive(Node node) {
        if (node instanceof Button
            || node instanceof TextField
            || node instanceof TextArea) {
            node.setOnMouseEntered(e -> playHover());
            node.setOnMouseClicked(e -> playClick());
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                bindRecursive(child);
            }
        }
    }
}
