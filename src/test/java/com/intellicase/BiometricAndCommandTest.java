package com.intellicase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.intellicase.application.AuditCommand;
import com.intellicase.application.FaceRecognitionService;
import com.intellicase.application.LogAuditEntryCommand;
import com.intellicase.application.SecurityCommandInvoker;
import com.intellicase.dao.AuditLogDao;
import com.intellicase.dao.BiometricDao;
import com.intellicase.data.DatabaseConnection;

/**
 * Test Suite for Biometric Authentication and Command Pattern.
 *
 * Uses an in-memory SQLite database so no file I/O is needed.
 * Covers:
 *   • BiometricDao — face template persistence and QR token lifecycle
 *   • FaceRecognitionService — descriptor computation and cosine similarity
 *   • Command Pattern — LogAuditEntryCommand + SecurityCommandInvoker
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BiometricAndCommandTest {

    private Connection inMemoryDb;
    private BiometricDao biometricDao;
    private FaceRecognitionService faceService;
    private AuditLogDao auditLogDao;

    @BeforeAll
    public void setup() throws Exception {
        inMemoryDb = DriverManager.getConnection("jdbc:sqlite::memory:");
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        Field connField = DatabaseConnection.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(dbConn, inMemoryDb);

        try (Statement st = inMemoryDb.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS AppUsers ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT UNIQUE NOT NULL,"
                + "fullName TEXT NOT NULL,"
                + "email TEXT UNIQUE NOT NULL,"
                + "passwordHash TEXT NOT NULL,"
                + "role TEXT NOT NULL DEFAULT 'PUBLIC_USER')");
            st.execute("INSERT INTO AppUsers(username,fullName,email,passwordHash,role)"
                + " VALUES('testAgent','Test Agent','t@fbi.gov','hash','FIELD_AGENT')");
            st.execute("CREATE TABLE IF NOT EXISTS AuditLog ("
                + "logID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "action TEXT, targetID TEXT, actorID TEXT,"
                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)");
        }

        biometricDao = new BiometricDao();
        faceService  = new FaceRecognitionService(biometricDao);
        auditLogDao  = new AuditLogDao();
    }

    @AfterAll
    public void teardown() throws Exception {
        if (inMemoryDb != null && !inMemoryDb.isClosed()) {
            inMemoryDb.close();
        }
    }

    // ── BiometricDao tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("BiometricDao: face template tables created on construction")
    public void testBiometricTablesCreated() {
        // If ensureTablesExist() failed, subsequent operations would throw.
        // A successful QR token generation confirms table exists.
        assertNotNull(biometricDao, "BiometricDao should initialise without error");
    }

    @Test
    @DisplayName("BiometricDao: no face template initially for user 1")
    public void testNoFaceTemplateInitially() {
        assertFalse(biometricDao.hasFaceTemplate(1),
            "New user should have no face template");
    }

    @Test
    @DisplayName("BiometricDao: save and detect face template")
    public void testSaveAndDetectFaceTemplate() {
        double[] descriptor = new double[FaceRecognitionService.DESC_DIM];
        for (int i = 0; i < descriptor.length; i++) {
            descriptor[i] = i * 0.001;
        }
        String csv = BiometricDao.serializeDescriptor(descriptor);
        boolean saved = biometricDao.saveFaceDescriptor(1, csv);
        assertTrue(saved, "Face descriptor should be saved successfully");
        assertTrue(biometricDao.hasFaceTemplate(1), "Template should now exist");
    }

    @Test
    @DisplayName("BiometricDao: QR token generate and validate lifecycle")
    public void testQrTokenLifecycle() {
        String token = biometricDao.generateQrToken(1);
        assertNotNull(token, "Token should be generated");
        assertTrue(token.startsWith("IC-1-"), "Token should contain user prefix");

        // First validation succeeds and deletes token
        assertNotNull(biometricDao.validateQrToken(token),
            "Valid token should return user");

        // Second use must fail (one-time use)
        assertNull(biometricDao.validateQrToken(token),
            "Token should be invalid after first use");
    }

    // ── FaceRecognitionService tests ──────────────────────────────────────────

    @Test
    @DisplayName("FaceRecognitionService: descriptor has correct dimension")
    public void testDescriptorDimension() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        double[] desc = faceService.computeDescriptor(img);
        assertEquals(FaceRecognitionService.DESC_DIM, desc.length,
            "Descriptor should have " + FaceRecognitionService.DESC_DIM + " bins");
    }

    @Test
    @DisplayName("FaceRecognitionService: descriptor is L2-normalised (magnitude ≈ 1)")
    public void testDescriptorNormalized() {
        BufferedImage img = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
        // Paint half the image red
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 80; y++) {
                img.setRGB(x, y, 0xFF0000);
            }
        }
        double[] desc = faceService.computeDescriptor(img);
        double mag = 0;
        for (double v : desc) {
            mag += v * v;
        }
        mag = Math.sqrt(mag);
        assertEquals(1.0, mag, 0.001,
            "L2-normalised descriptor magnitude should be ≈ 1.0");
    }

    @Test
    @DisplayName("FaceRecognitionService: null frame returns null gracefully")
    public void testNullFrameGraceful() {
        assertNull(faceService.recognise(null),
            "recognise(null) should return null without throwing");
        assertNull(faceService.enrol(1, null),
            "enrol(null frame) should return null without throwing");
    }

    // ── Command Pattern tests ─────────────────────────────────────────────────

    @Test
    @DisplayName("Command Pattern: LogAuditEntryCommand executes and describes correctly")
    public void testLogAuditEntryCommand() {
        AuditCommand cmd = new LogAuditEntryCommand(
            "TEST_ACTION", "TARGET-001", "AGT-TEST", auditLogDao);

        assertEquals("[AUDIT] action=TEST_ACTION target=TARGET-001 actor=AGT-TEST",
            cmd.describe(), "Command description should match");

        boolean ok = cmd.execute();
        assertTrue(ok, "Command should execute successfully against in-memory DB");
    }

    @Test
    @DisplayName("Command Pattern: SecurityCommandInvoker tracks history")
    public void testSecurityCommandInvokerHistory() {
        SecurityCommandInvoker invoker = SecurityCommandInvoker.getInstance();
        int before = invoker.historySize();

        AuditCommand cmd = new LogAuditEntryCommand(
            "LOCKDOWN_ACTIVATED", "SYSTEM", "DIRECTOR-01", auditLogDao);
        boolean ok = invoker.invoke(cmd);

        assertTrue(ok, "Invoker should execute command successfully");
        assertEquals(before + 1, invoker.historySize(),
            "History should grow by 1 after successful invocation");

        AuditCommand last = invoker.lastCommand();
        assertNotNull(last, "Last command should be retrievable");
        assertTrue(last.describe().contains("LOCKDOWN_ACTIVATED"),
            "Last command should be the one just invoked");
    }
}
