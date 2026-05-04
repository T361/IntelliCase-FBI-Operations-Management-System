package com.intellicase.application;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamException;

import javafx.application.Platform;

/**
 * GoF Singleton wrapping Sarxos webcam-capture.
 * Streams BufferedImage frames to registered listeners on a background thread,
 * then optionally dispatches a post-process callback to JavaFX thread.
 *
 * GRASP Low Coupling: controllers depend only on this service, not on Sarxos.
 */
public final class WebcamService {

    /** Target frame interval in milliseconds (≈15 fps). */
    private static final int FRAME_INTERVAL_MS = 67;

    private static WebcamService instance;

    private Webcam webcam;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> captureTask;

    /** Called on background thread with every new frame. */
    private Consumer<BufferedImage> rawFrameListener;
    /** Called on JavaFX thread with every new frame. */
    private Consumer<BufferedImage> fxFrameListener;

    private volatile boolean running = false;

    private WebcamService() { }

    public static synchronized WebcamService getInstance() {
        if (instance == null) {
            instance = new WebcamService();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LIFECYCLE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Open the default webcam and start streaming frames to the supplied
     * listeners.  Safe to call from any thread.
     *
     * @param onFrame  called on the background thread with each new frame;
     *                 may be {@code null}
     * @param onFxFrame called on the JavaFX Application Thread; may be null
     */
    public synchronized void start(Consumer<BufferedImage> onFrame,
                                   Consumer<BufferedImage> onFxFrame) {
        if (running) {
            stop();
        }

        this.rawFrameListener = onFrame;
        this.fxFrameListener  = onFxFrame;

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WebcamCapture");
            t.setDaemon(true);
            return t;
        });

        scheduler.execute(this::openCamera);
    }

    /** Stop capturing and close the webcam. */
    public synchronized void stop() {
        running = false;
        if (captureTask != null) {
            captureTask.cancel(true);
            captureTask = null;
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        if (webcam != null && webcam.isOpen()) {
            try {
                webcam.close();
            } catch (Exception ex) {
                // ignore
            }
            webcam = null;
        }
    }

    public boolean isRunning() {
        return running;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INTERNAL
    // ─────────────────────────────────────────────────────────────────────────

    private void openCamera() {
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                System.err.println("[WebcamService] No webcam found.");
                return;
            }
            webcam.open();
            running = true;
            captureTask = scheduler.scheduleAtFixedRate(
                this::captureFrame, 0, FRAME_INTERVAL_MS, TimeUnit.MILLISECONDS);
        } catch (WebcamException ex) {
            System.err.println("[WebcamService] Cannot open webcam: " + ex.getMessage());
        }
    }

    private void captureFrame() {
        if (!running || webcam == null || !webcam.isOpen()) {
            return;
        }
        try {
            BufferedImage frame = webcam.getImage();
            if (frame == null) {
                return;
            }

            if (rawFrameListener != null) {
                rawFrameListener.accept(frame);
            }
            if (fxFrameListener != null) {
                Platform.runLater(() -> fxFrameListener.accept(frame));
            }
        } catch (Exception ex) {
            // Frame grab can fail transiently — ignore single failures
        }
    }
}
