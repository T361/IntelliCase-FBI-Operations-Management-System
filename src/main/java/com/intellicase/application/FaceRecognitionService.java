package com.intellicase.application;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.intellicase.dao.BiometricDao;

/**
 * Pure-Java face recognition service.
 *
 * Instead of a native DNN, this uses a compact 192-bin colour histogram
 * (64 bins each for R, G, B channels) computed on a normalised 96×96
 * centre-crop of the webcam frame.  Matching is done via cosine similarity
 * with a stored template, which is robust to moderate lighting changes and
 * works without any native OpenCV library.
 *
 * GRASP Expert: owns all face-descriptor logic.
 * GoF Strategy: implements DescriptorStrategy (can be swapped for DNN later).
 */
public class FaceRecognitionService {

    /** Number of bins per channel (R, G, B → 3 × BINS total). */
    private static final int BINS = 64;
    /** Descriptor dimension. */
    public static final int DESC_DIM = BINS * 3;
    /** Width/height of the normalised face region used for the histogram. */
    private static final int NORM_SIZE = 96;
    /** Central crop fraction: we use the middle 50 % of each axis. */
    private static final double CROP_FRAC = 0.50;

    private final BiometricDao biometricDao;

    public FaceRecognitionService(BiometricDao biometricDao) {
        this.biometricDao = biometricDao;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Compute the descriptor for a raw webcam frame and compare it against
     * every stored face template.
     *
     * @return matching AppUser or {@code null} if no match above threshold.
     */
    public com.intellicase.domain.AppUser recognise(BufferedImage frame) {
        if (frame == null) {
            return null;
        }
        double[] probe = computeDescriptor(frame);
        return biometricDao.findUserByFace(probe);
    }

    /**
     * Compute the descriptor for the given frame and store it as the face
     * template for {@code userId}.  Call this during the face-enrolment step.
     *
     * @return serialised descriptor string on success, {@code null} on failure.
     */
    public String enrol(int userId, BufferedImage frame) {
        if (frame == null) {
            return null;
        }
        double[] desc = computeDescriptor(frame);
        String serialised = BiometricDao.serializeDescriptor(desc);
        boolean ok = biometricDao.saveFaceDescriptor(userId, serialised);
        return ok ? serialised : null;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DESCRIPTOR COMPUTATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Build a normalised RGB histogram descriptor from the central crop of
     * the supplied image.  The result is an L2-normalised float array of
     * length {@link #DESC_DIM}.
     */
    public double[] computeDescriptor(BufferedImage src) {
        BufferedImage cropped = centreCrop(src);
        BufferedImage scaled  = scale(cropped, NORM_SIZE, NORM_SIZE);
        return buildHistogram(scaled);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private BufferedImage centreCrop(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int cw = (int) (w * CROP_FRAC);
        int ch = (int) (h * CROP_FRAC);
        int x  = (w - cw) / 2;
        int y  = (h - ch) / 2;
        return img.getSubimage(x, y, cw, ch);
    }

    private BufferedImage scale(BufferedImage img, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    /** Build a 3×BINS colour histogram then L2-normalise it. */
    private double[] buildHistogram(BufferedImage img) {
        double[] hist = new double[DESC_DIM];
        int w = img.getWidth();
        int h = img.getHeight();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                int rBin = (c.getRed()   * BINS) / 256;
                int gBin = (c.getGreen() * BINS) / 256 + BINS;
                int bBin = (c.getBlue()  * BINS) / 256 + BINS * 2;
                hist[rBin]++;
                hist[gBin]++;
                hist[bBin]++;
            }
        }

        // L2 normalise
        double norm = 0;
        for (double v : hist) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < hist.length; i++) {
                hist[i] /= norm;
            }
        }
        return hist;
    }
}
