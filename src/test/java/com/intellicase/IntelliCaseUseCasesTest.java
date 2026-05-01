package com.intellicase;

import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import com.intellicase.domain.*;
import com.intellicase.application.*;
import com.intellicase.dao.*;
import com.intellicase.data.DatabaseConnection;
import com.intellicase.application.EvidenceController.HandshakeResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive Test Suite for all major Use Cases in the IntelliCase System.
 * Focuses on business logic, rule enforcement, and state changes.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntelliCaseUseCasesTest {

    private Connection inMemoryDb;
    private CaseController caseCtrl;
    private EvidenceController evCtrl;
    private SecurityController secCtrl;
    
    private AgentDao agentDao;
    private CaseFileDao caseDao;
    private EvidenceDao evidenceDao;
    private AppUserDao userDao;

    @BeforeAll
    public void setupInMemoryDatabase() throws Exception {
        inMemoryDb = DriverManager.getConnection("jdbc:sqlite::memory:");
        
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        Field connField = DatabaseConnection.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(dbConn, inMemoryDb);

        // Scaffold Tables
        try (Statement stmt = inMemoryDb.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Agents (agentID TEXT PRIMARY KEY, name TEXT, clearanceLevel INTEGER, currentLoadScore INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS AuditLog (logID INTEGER PRIMARY KEY AUTOINCREMENT, action TEXT, targetID TEXT, timestamp TEXT, actorID TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS ShadowProfiles (profileID TEXT PRIMARY KEY, alias TEXT, encryptedData TEXT, caseID TEXT, creatorAgentId TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Evidence (evidenceID TEXT PRIMARY KEY, caseID TEXT, status TEXT, custodian TEXT, integrityHash TEXT, sensitivityLevel INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Cases (caseID TEXT PRIMARY KEY, status TEXT, description TEXT, priority TEXT, location TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS AppUsers (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, fullName TEXT NOT NULL, email TEXT UNIQUE NOT NULL, passwordHash TEXT NOT NULL, role TEXT NOT NULL DEFAULT 'PUBLIC_USER')");
        }

        caseCtrl = new CaseController();
        evCtrl = new EvidenceController();
        secCtrl = new SecurityController();
        
        agentDao = new AgentDao();
        caseDao = new CaseFileDao();
        evidenceDao = new EvidenceDao();
        userDao = new AppUserDao();
        
        seedBaseData();
    }

    private void seedBaseData() {
        agentDao.create(new Agent("AGT-100", "Fox Mulder", 5, 20));
        agentDao.create(new Agent("AGT-200", "Dana Scully", 2, 80));
        
        caseDao.create(new CaseFile("CASE-100", "OPEN", "X-Files Investigation", "HIGH", "Washington"));
        evidenceDao.create(new Evidence("EVD-100", "CASE-100", "SECURED", "AGT-100", "hash123", 3));
        
        userDao.register("director_test", "Director Smith", "dir@fbi.gov", "password123", "FBI_DIRECTOR");
    }

    @BeforeEach
    public void resetSystemState() {
        SystemState.getInstance().deactivateLockdown();
    }

    @AfterAll
    public void tearDownDatabase() throws Exception {
        if (inMemoryDb != null && !inMemoryDb.isClosed()) {
            inMemoryDb.close();
        }
    }

    @Test
    @DisplayName("UC-11: Create Smart Case")
    public void testCreateSmartCase() {
        CaseFile newCase = caseCtrl.createSmartCase("OPEN", "Bank Heist", "CRITICAL", "New York", "AGT-100");
        assertNotNull(newCase, "Case should be created successfully");
        assertEquals("OPEN", newCase.getStatus());
        assertEquals("CRITICAL", newCase.getPriority());
        
        CaseFile retrieved = caseDao.findById(newCase.getCaseId());
        assertNotNull(retrieved, "Case should be persisted to DB");
        assertEquals("Bank Heist", retrieved.getDescription());
    }

    @Test
    @DisplayName("UC-01: Initiate Digital Handshake (Success & Clearance Denied)")
    public void testInitiateDigitalHandshake() {
        // High clearance agent receiving low sensitivity evidence (Success)
        HandshakeResult result1 = evCtrl.initiateDigitalHandshake("EVD-100", "AGT-100", "SIGNATURE_VALID", "AGT-200");
        assertEquals(HandshakeResult.SUCCESS, result1, "Handshake should succeed for authorized agent");
        
        Evidence ev = evidenceDao.findById("EVD-100");
        assertEquals("IN_TRANSIT", ev.getStatus());
        assertEquals("AGT-100", ev.getCustodian());

        // Low clearance agent (level 2) receiving high sensitivity evidence (level 3) (Denied)
        // Reset evidence status first
        evidenceDao.updateStatus("EVD-100", "SECURED");
        HandshakeResult result2 = evCtrl.initiateDigitalHandshake("EVD-100", "AGT-200", "SIGNATURE_VALID", "AGT-100");
        assertEquals(HandshakeResult.UNAUTHORIZED, result2, "Handshake should be denied due to low clearance");
    }

    @Test
    @DisplayName("UC-05: Assign Agent Smart Load Score")
    public void testAssignAgentSmartLoad() {
        // Agent 100 has load 20. Active case count = 1. New load = 20 + 1*10 = 30. (Threshold = 100)
        boolean success = evCtrl.assignAgentSmartLoad("AGT-100", 1, "ADMIN");
        assertTrue(success, "Agent assignment should succeed");
        
        Agent updated = agentDao.findById("AGT-100");
        assertEquals(30, updated.getCurrentLoadScore(), "Load score should increase properly");

        // Overload the agent
        boolean fail = evCtrl.assignAgentSmartLoad("AGT-100", 10, "ADMIN"); // 30 + 100 = 130 > 100
        assertFalse(fail, "Assignment should fail if load score exceeds threshold");
    }

    @Test
    @DisplayName("UC-07: Create and View Shadow Profile")
    public void testShadowProfile() {
        SecurityController.ShadowProfileResult result = secCtrl.createShadowProfile("SP-999", "DeepThroat", "Confidential Intel", "CASE-100", "AGT-100");
        assertEquals(SecurityController.ShadowProfileResult.SUCCESS, result);
        
        // Try creating with duplicate alias
        SecurityController.ShadowProfileResult dupResult = secCtrl.createShadowProfile("SP-998", "DeepThroat", "Intel", "CASE-100", "AGT-100");
        assertEquals(SecurityController.ShadowProfileResult.ALIAS_IN_USE, dupResult);
        
        // Authorized view
        String details = secCtrl.viewShadowProfile("SP-999", "AGT-100");
        assertTrue(details.contains("Decrypted Details"), "Authorized agent should see decrypted intel");
        
        // Unauthorized view
        String blocked = secCtrl.viewShadowProfile("SP-999", "AGT-200");
        assertTrue(blocked.contains("ACCESS DENIED"), "Unauthorized agent should be blocked");
    }

    @Test
    @DisplayName("UC-09: Promote Security Clearance")
    public void testPromoteClearance() {
        Agent agent = agentDao.findById("AGT-200");
        int originalClearance = agent.getClearanceLevel();
        
        boolean success = secCtrl.promoteSecurityClearance("AGT-200", "ADMIN");
        assertTrue(success);
        
        Agent updated = agentDao.findById("AGT-200");
        assertEquals(originalClearance + 1, updated.getClearanceLevel(), "Clearance level should increment by 1");
    }

    @Test
    @DisplayName("UC-08 & UC-15: System Lockdown Lifecycle")
    public void testLockdownLifecycle() {
        // UC-08 Step 1
        assertTrue(secCtrl.performUpsCheck(), "UPS Check should pass");
        
        // UC-08 Step 2 & 3
        SecurityController.LockdownAuthResult auth = secCtrl.authenticateDirector("director_test", "password123");
        assertEquals(SecurityController.LockdownAuthResult.SUCCESS, auth, "Director auth should succeed");
        
        boolean locked = secCtrl.activateAuditLockdown("director_test");
        assertTrue(locked, "Lockdown should activate");
        assertTrue(SystemState.getInstance().isLockdownActive());
        
        // Test that modifications are blocked while locked down
        CaseFile blockedCase = caseCtrl.createSmartCase("OPEN", "Blocked Case", "LOW", "HQ", "AGT-100");
        assertNull(blockedCase, "Case creation should be blocked during lockdown");
        
        // UC-15: Deactivate Lockdown
        boolean unlocked = caseCtrl.deactivateAuditLockdown("NCIS-X-99", "director_test");
        assertTrue(unlocked, "Lockdown should deactivate with correct code");
        assertFalse(SystemState.getInstance().isLockdownActive());
    }

    @Test
    @DisplayName("UC-14: Secure Evidence Inventory Audit")
    public void testEvidenceInventoryAudit() {
        // The DAO handles fetching. CaseController returns the list.
        List<Evidence> allEvidence = caseCtrl.secureEvidenceAudit("AGT-100");
        assertNotNull(allEvidence);
        assertFalse(allEvidence.isEmpty());
        
        List<Evidence> caseEvidence = caseCtrl.secureEvidenceAudit("AGT-100", "CASE-100");
        assertEquals(1, caseEvidence.size());
    }
}
