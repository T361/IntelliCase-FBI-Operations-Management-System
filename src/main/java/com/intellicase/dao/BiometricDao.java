package com.intellicase.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.intellicase.data.DatabaseConnection;
import com.intellicase.domain.AppUser;

/**
 * DAO for biometric authentication data.
 * Stores face feature descriptors (base64-encoded float arrays) and
 * one-time QR login tokens per AppUser.
 */
public class BiometricDao {

    private static final double FACE_MATCH_THRESHOLD = 0.82;

    private final DatabaseConnection db;

    public BiometricDao() {
        this.db = DatabaseConnection.getInstance();
        ensureTablesExist();
    }

    /** Create biometric tables if they do not already exist. */
    public final void ensureTablesExist() {
        String faceDdl = "CREATE TABLE IF NOT EXISTS FaceTemplates ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "userId INTEGER NOT NULL,"
            + "descriptor TEXT NOT NULL,"
            + "createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(userId) REFERENCES AppUsers(id))";

        String qrDdl = "CREATE TABLE IF NOT EXISTS QrTokens ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "userId INTEGER NOT NULL UNIQUE,"
            + "token TEXT NOT NULL,"
            + "expiresAt INTEGER NOT NULL,"
            + "FOREIGN KEY(userId) REFERENCES AppUsers(id))";

        try (Statement st = db.getConnection().createStatement()) {
            st.execute(faceDdl);
            st.execute(qrDdl);
        } catch (SQLException ex) {
            System.err.println("[BiometricDao] Table creation failed: " + ex.getMessage());
        }
    }

    // ─── FACE TEMPLATES ─────────────────────────────────────────────────────

    /**
     * Save or replace a face descriptor for the given userId.
     * Descriptor is a comma-separated list of float values.
     */
    public boolean saveFaceDescriptor(int userId, String descriptor) {
        String sql = "INSERT OR REPLACE INTO FaceTemplates (userId, descriptor) VALUES (?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, descriptor);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            System.err.println("[BiometricDao] saveFaceDescriptor failed: " + ex.getMessage());
            return false;
        }
    }

    /** Returns true if the user already has a stored face template. */
    public boolean hasFaceTemplate(int userId) {
        String sql = "SELECT COUNT(*) FROM FaceTemplates WHERE userId = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    /**
     * Find an AppUser whose stored face descriptor matches the given probe.
     * Computes cosine similarity between stored and probe descriptor vectors.
     * Returns null if no match exceeds the threshold.
     */
    public AppUser findUserByFace(double[] probe) {
        String sql = "SELECT u.id, u.username, u.fullName, u.email, u.role, ft.descriptor"
            + " FROM FaceTemplates ft"
            + " JOIN AppUsers u ON u.id = ft.userId";
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            AppUser best = null;
            double bestSim = -1;
            while (rs.next()) {
                String stored = rs.getString("descriptor");
                double[] template = parseDescriptor(stored);
                double sim = cosineSimilarity(probe, template);
                if (sim > bestSim) {
                    bestSim = sim;
                    if (sim >= FACE_MATCH_THRESHOLD) {
                        best = new AppUser(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("fullName"),
                            rs.getString("email"),
                            rs.getString("role")
                        );
                    }
                }
            }
            return best;
        } catch (SQLException ex) {
            System.err.println("[BiometricDao] findUserByFace failed: " + ex.getMessage());
            return null;
        }
    }

    // ─── QR TOKENS ───────────────────────────────────────────────────────────

    /**
     * Generate and persist a QR token for the given user.
     * Token is a UUID-based string; TTL is 5 minutes.
     * Returns the token string to be encoded in the QR image.
     */
    public String generateQrToken(int userId) {
        String token = "IC-" + userId + "-" + java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
        long expiresAt = System.currentTimeMillis() + (5L * 60 * 1000); // 5 min
        String sql = "INSERT OR REPLACE INTO QrTokens (userId, token, expiresAt) VALUES (?, ?, ?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, token);
            ps.setLong(3, expiresAt);
            ps.executeUpdate();
            return token;
        } catch (SQLException ex) {
            System.err.println("[BiometricDao] generateQrToken failed: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Validate a QR token scanned from camera.
     * Returns the matching AppUser if token is valid and not expired; else null.
     * Token is deleted after a successful match (one-time use).
     */
    public AppUser validateQrToken(String token) {
        String sql = "SELECT u.id, u.username, u.fullName, u.email, u.role, qt.expiresAt"
            + " FROM QrTokens qt"
            + " JOIN AppUsers u ON u.id = qt.userId"
            + " WHERE qt.token = ?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long expiresAt = rs.getLong("expiresAt");
                    if (System.currentTimeMillis() > expiresAt) {
                        deleteToken(token);
                        return null; // expired
                    }
                    AppUser user = new AppUser(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("fullName"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                    deleteToken(token); // one-time use
                    return user;
                }
            }
        } catch (SQLException ex) {
            System.err.println("[BiometricDao] validateQrToken failed: " + ex.getMessage());
        }
        return null;
    }

    private void deleteToken(String token) {
        try (PreparedStatement ps = db.getConnection()
                .prepareStatement("DELETE FROM QrTokens WHERE token = ?")) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException ex) {
            // non-critical
        }
    }

    // ─── MATH HELPERS ────────────────────────────────────────────────────────

    /** Parse a comma-separated descriptor string into a double[]. */
    public static double[] parseDescriptor(String csv) {
        String[] parts = csv.split(",");
        double[] vals = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vals[i] = Double.parseDouble(parts[i].trim());
        }
        return vals;
    }

    /** Serialize a double[] to comma-separated string. */
    public static String serializeDescriptor(double[] vals) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(vals[i]);
        }
        return sb.toString();
    }

    /** Cosine similarity between two equal-length vectors. Range [-1, 1]. */
    private double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) {
            return -1;
        }
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return -1;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
