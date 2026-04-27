package com.intellicase.presentation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import javafx.scene.Node;

/**
 * Synthesizes runtime audio tones for UI feedback using javax.sound.sampled.
 * No external WAV files needed. Provides hover blips, click tones, and error buzzes.
 */
public final class AudioFeedbackManager {
    private static byte[] hoverTone;
    private static byte[] clickTone;
    private static byte[] errorTone;
    private static boolean initialized = false;

    private AudioFeedbackManager() {
    }

    private static synchronized void init() {
        if (initialized) {
            return;
        }
        hoverTone = synthesizeTone(800, 50, 0.3);
        clickTone = synthesizeTone(600, 80, 0.4);
        errorTone = synthesizeTone(200, 150, 0.5);
        initialized = true;
    }

    private static byte[] synthesizeTone(int frequencyHz, int durationMs, double volume) {
        int sampleRate = 16000;
        int numSamples = (sampleRate * durationMs) / 1000;
        byte[] buffer = new byte[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i * frequencyHz / sampleRate;
            buffer[i] = (byte) (Math.sin(angle) * 127.0 * volume);
        }
        return buffer;
    }

    private static void playTone(byte[] toneData) {
        if (toneData == null) {
            return;
        }
        new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(16000, 8, 1, true, false);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(toneData, 0, toneData.length);
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                AudioInputStream ais = new AudioInputStream(bais, format, toneData.length);
                Clip clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
            } catch (Exception | UnsatisfiedLinkError ex) {
                // Silent fail — audio is non-critical and Linux PipeWire Segfaults are swallowed
            }
        }, "audio-feedback").start();
    }

    public static void playHover() {
        init();
        playTone(hoverTone);
    }

    public static void playClick() {
        init();
        playTone(clickTone);
    }

    public static void playError() {
        init();
        playTone(errorTone);
    }

    public static void attachTo(Node node) {
        node.setOnMouseEntered(e -> playHover());
        node.setOnMouseClicked(e -> playClick());
    }
}
