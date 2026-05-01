package com.intellicase.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.intellicase.data.DatabaseConnection;
import com.intellicase.domain.AppUser;

/**
 * DAO for AppUser authentication and registration.
 * Passwords stored as SHA-256 hex digests.
 */
public class AppUserDao {
    private final DatabaseConnection db;

    public AppUserDao() {
        this.db = DatabaseConnection.getInstance();
        ensureTableExists();
    }

    /** Create the AppUsers table if it does not already exist and seed defaults. */
    public final void ensureTableExists() {
        String ddl = "CREATE TABLE IF NOT EXISTS AppUsers ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "username TEXT UNIQUE NOT NULL,"
            + "fullName TEXT NOT NULL,"
            + "email TEXT UNIQUE NOT NULL,"
            + "passwordHash TEXT NOT NULL,"
            + "role TEXT NOT NULL DEFAULT 'PUBLIC_USER')";
        try (Statement st = db.getConnection().createStatement()) {
            st.execute(ddl);
            seedDefaultUsers();
        } catch (SQLException ex) {
            System.err.println("[AppUserDao] Table creation failed: " + ex.getMessage());
        }
    }

    private void seedDefaultUsers() {
        String check = "SELECT COUNT(*) FROM AppUsers";
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(check)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        } catch (SQLException ex) {
            return;
        }
        insertIfAbsent("director", "Director J. Edgar", "director@fbi.gov",
            hash("Director@2026"), "FBI_DIRECTOR");
        insertIfAbsent("supervisor1", "Supervisor Morgan", "morgan@fbi.gov",
            hash("Supervisor@2026"), "CASE_SUPERVISOR");
        insertIfAbsent("agent1", "Special Agent Fox", "fox@fbi.gov",
            hash("Agent@2026"), "FIELD_AGENT");
        insertIfAbsent("analyst1", "Analyst Reyes", "reyes@fbi.gov",
            hash("Analyst@2026"), "INTELLIGENCE_ANALYST");
        insertIfAbsent("public1", "John Citizen", "john@public.gov",
            hash("Public@2026"), "PUBLIC_USER");
    }

    private void insertIfAbsent(String username, String fullName,
                                String email, String pwHash, String role) {
        String sql = "INSERT OR IGNORE INTO AppUsers "
            + "(username,fullName,email,passwordHash,role) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setString(4, pwHash);
            ps.setString(5, role);
            ps.executeUpdate();
        } catch (SQLException ex) {
            System.err.println("[AppUserDao] Seed failed: " + ex.getMessage());
        }
    }

    /** Authenticate by username + plain password. Returns null on failure. */
    public AppUser authenticate(String username, String password) {
        String sql = "SELECT id,username,fullName,email,role FROM AppUsers"
            + " WHERE username=? AND passwordHash=?";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AppUser(rs.getInt("id"), rs.getString("username"),
                        rs.getString("fullName"), rs.getString("email"),
                        rs.getString("role"));
                }
            }
        } catch (SQLException ex) {
            System.err.println("[AppUserDao] Auth failed: " + ex.getMessage());
        }
        return null;
    }

    /** Register a new user. Returns false if username/email already taken. */
    public boolean register(String username, String fullName,
                            String email, String password, String role) {
        String sql = "INSERT INTO AppUsers (username,fullName,email,passwordHash,role)"
            + " VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, fullName);
            ps.setString(3, email);
            ps.setString(4, hash(password));
            ps.setString(5, role);
            ps.executeUpdate();
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    /** Check if an FBI Director already exists in the database. */
    public boolean hasDirector() {
        String sql = "SELECT COUNT(*) FROM AppUsers WHERE role = 'FBI_DIRECTOR'";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            System.err.println("[AppUserDao] Error checking director: " + ex.getMessage());
        }
        return false;
    }

    /** SHA-256 hex digest of the given plain-text input. */
    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            return input;
        }
    }
}
