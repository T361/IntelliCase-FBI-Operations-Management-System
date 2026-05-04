package com.intellicase.application;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import com.intellicase.dao.BiometricDao;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * GRASP Expert for all QR-code operations:
 *   • generate a QR image from a token string
 *   • decode a QR token from a BufferedImage frame
 *   • validate the decoded token against the database
 *
 * GoF Singleton — one shared instance manages the ZXing reader/writer.
 */
public final class QrCodeService {

    private static QrCodeService instance;

    private final BiometricDao biometricDao;
    private final QRCodeWriter writer;
    private final MultiFormatReader reader;

    private QrCodeService() {
        this.biometricDao = new BiometricDao();
        this.writer = new QRCodeWriter();

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        this.reader = new MultiFormatReader();
        this.reader.setHints(hints);
    }

    public static synchronized QrCodeService getInstance() {
        if (instance == null) {
            instance = new QrCodeService();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QR GENERATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generate a one-time QR login code for the given user.
     * The token is stored in the database with a 5-minute TTL.
     *
     * @param userId  numeric user ID
     * @param size    pixel dimension of the generated square QR image
     * @return JavaFX {@link Image}, or {@code null} on failure
     */
    public Image generateLoginQr(int userId, int size) {
        String token = biometricDao.generateQrToken(userId);
        if (token == null) {
            return null;
        }
        return encodeToFxImage(token, size);
    }

    /**
     * Encode any text string into a QR JavaFX Image.
     */
    public Image encodeToFxImage(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage bi = MatrixToImageWriter.toBufferedImage(matrix);
            return SwingFXUtils.toFXImage(bi, null);
        } catch (Exception ex) {
            System.err.println("[QrCodeService] encode failed: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Encode a token into a raw {@link BufferedImage} (for tests / preview).
     */
    public BufferedImage encodeToBufferedImage(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (Exception ex) {
            System.err.println("[QrCodeService] encodeBI failed: " + ex.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // QR DECODING
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Attempt to decode a QR code from a webcam {@link BufferedImage} frame.
     *
     * @return the decoded token string, or {@code null} if none found
     */
    public String decodeFrame(BufferedImage frame) {
        if (frame == null) {
            return null;
        }
        try {
            int[] pixels = frame.getRGB(0, 0, frame.getWidth(), frame.getHeight(), null, 0, frame.getWidth());
            RGBLuminanceSource src = new RGBLuminanceSource(frame.getWidth(), frame.getHeight(), pixels);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(src));
            Result result = reader.decode(bitmap);
            return result.getText();
        } catch (NotFoundException ex) {
            return null; // no QR in this frame — expected during scanning
        } catch (Exception ex) {
            System.err.println("[QrCodeService] decode error: " + ex.getMessage());
            return null;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TOKEN VALIDATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Validate a decoded token string against the database.
     *
     * @return matching {@link com.intellicase.domain.AppUser} or {@code null}
     */
    public com.intellicase.domain.AppUser validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return biometricDao.validateQrToken(token);
    }

    /** Expose DAO so controllers can call generateQrToken directly. */
    public BiometricDao getBiometricDao() {
        return biometricDao;
    }
}
