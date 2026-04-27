package com.intellicase;

import org.junit.jupiter.api.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import com.intellicase.domain.*;
import com.intellicase.application.*;
import com.intellicase.dao.*;
import com.intellicase.data.DatabaseConnection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 2 Unit Test Generation: Extreme QA Validation against SDA Project Rubric.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IntelliCaseBackendTest {

    private Connection inMemoryDb;

    @BeforeAll
    public void setupInMemoryDatabase() throws Exception {
        // Rubric Item 6: Database Integrity via in-memory DB isolation.
        inMemoryDb = DriverManager.getConnection("jdbc:sqlite::memory:");
        
        DatabaseConnection dbConn = DatabaseConnection.getInstance();
        Field connField = DatabaseConnection.class.getDeclaredField("connection");
        connField.setAccessible(true);
        connField.set(dbConn, inMemoryDb);

        // Scaffold Tables
        try (Statement stmt = inMemoryDb.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS Agents (agentID TEXT PRIMARY KEY, name TEXT, clearanceLevel INTEGER, currentLoadScore INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS AuditLogs (logId TEXT PRIMARY KEY, actionType TEXT, targetId TEXT, timestamp TEXT, actorId TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS ShadowProfiles (profileId TEXT PRIMARY KEY, alias TEXT, encryptedData TEXT, caseId TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS Evidence (evidenceId TEXT PRIMARY KEY, description TEXT, status TEXT, caseId TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS CaseFiles (caseId TEXT PRIMARY KEY, status TEXT, description TEXT, priority TEXT, location TEXT)");
        }
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

    // --- RUBRIC ITEM 4: OOP PRINCIPLES ---

    @Test
    public void testDomainEncapsulation() {
        Class<?>[] classes = {CaseFile.class, Evidence.class, Agent.class, ShadowProfile.class, AuditLogEntry.class};
        for (Class<?> clazz : classes) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    assertTrue(Modifier.isPrivate(field.getModifiers()), 
                        "FATAL ERROR: Field " + field.getName() + " in " + clazz.getSimpleName() + " is not private!");
                }
            }
        }
    }

    @Test
    public void testPolymorphismInStrategies() {
        EvidenceController controller = new EvidenceController();
        // Strategy pattern ensures we can swap out implementation without errors.
        assertDoesNotThrow(() -> controller.setLoadScoreStrategy(new DefaultLoadScoreStrategy()));
        
        SecurityController secController = new SecurityController();
        assertDoesNotThrow(() -> secController.setEncryptionStrategy(new Aes256SimulationStrategy()));
    }


    // --- RUBRIC ITEM 5: BUSINESS LOGIC (Edge Cases & Fault Tolerance) ---

    @Test
    public void testBusinessLogicNullHandlingAndEmptyStrings() {
        CaseController caseCtrl = new CaseController();
        SecurityController secCtrl = new SecurityController();
        EvidenceController evCtrl = new EvidenceController();

        assertDoesNotThrow(() -> {
            caseCtrl.createSmartCase(null, "", null, null, null);
            caseCtrl.secureEvidenceAudit("");
            caseCtrl.deactivateAuditLockdown(null, null);

            secCtrl.createShadowProfile(null, null, "", null, null);
            secCtrl.activateAuditLockdown(null);
            secCtrl.promoteSecurityClearance("", null);

            evCtrl.initiateDigitalHandshake(null, null);
            evCtrl.assignAgentSmartLoad(null, -500, null);
            evCtrl.viewAuditTrail(null);
        }, "FATAL ERROR: Controllers failed to handle edge cases gracefully, JVM crash detected.");
        
        // Ensure invalid override code rejects lockdown override without crashing
        assertFalse(caseCtrl.deactivateAuditLockdown("INVALID-CODE", "actor1"));
    }

    @Test
    public void testSystemLockdownRejectsOperations() {
        SecurityController secCtrl = new SecurityController();
        CaseController caseCtrl = new CaseController();
        
        secCtrl.activateAuditLockdown("System");
        assertTrue(SystemState.getInstance().isLockdownActive());

        CaseFile created = caseCtrl.createSmartCase("OPEN", "Test", "HIGH", "HQ", "Agent1");
        assertNull(created, "Business Logic Flaw: Case was created while lockdown was active.");
        
        boolean promoted = secCtrl.promoteSecurityClearance("A123", "Admin");
        assertFalse(promoted, "Business Logic Flaw: Clearance promoted while lockdown was active.");
    }


    // --- RUBRIC ITEM 6: DATABASE INTEGRITY ---

    @Test
    public void testDaoCrudOperationsRobustness() {
        AgentDao agentDao = new AgentDao();
        Agent agent = new Agent("A-999", "John Doe", 3, 50);
        
        // Write test
        agentDao.create(agent);
        
        // Read test
        Agent retrieved = agentDao.findById("A-999");
        assertNotNull(retrieved, "Database Integrity Flaw: Agent not retrieved after insert.");
        assertEquals("John Doe", retrieved.getName());

        // Update test
        agentDao.updateClearance("A-999", 5);
        retrieved = agentDao.findById("A-999");
        assertEquals(5, retrieved.getClearanceLevel());

        // SQL Injection resistance / Edge case handling test
        Agent malicious = agentDao.findById("' OR 1=1 --");
        assertNull(malicious, "SQL Injection vulnerability suspected.");
    }

    @Test
    public void testAuditLogInsertion() {
        AuditLogDao dao = new AuditLogDao();
        AuditLogEntry entry = new AuditLogEntry("TEST_ACTION", "TARGET_1", "ACTOR_1");
        
        assertDoesNotThrow(() -> dao.create(entry));
        
        List<AuditLogEntry> logs = dao.findByTargetId("TARGET_1");
        assertFalse(logs.isEmpty());
        assertEquals("TEST_ACTION", logs.get(0).getActionType());
    }


    // --- RUBRIC ITEM 10: DESIGN PATTERNS ---

    @Test
    public void testSingletonGoFImplementation() {
        DatabaseConnection instance1 = DatabaseConnection.getInstance();
        DatabaseConnection instance2 = DatabaseConnection.getInstance();
        
        assertSame(instance1, instance2, "Design Pattern Violation: DatabaseConnection is not a strict GoF Singleton.");
        
        // Verify constructor is private
        assertTrue(Modifier.isPrivate(DatabaseConnection.class.getDeclaredConstructors()[0].getModifiers()), 
            "Design Pattern Violation: DatabaseConnection constructor must be private.");
            
        SystemState state1 = SystemState.getInstance();
        SystemState state2 = SystemState.getInstance();
        assertSame(state1, state2, "Design Pattern Violation: SystemState is not a Singleton.");
    }

    @Test
    public void testFactoryGoFImplementation() {
        CaseFactory factory = new CaseFactory();
        CaseFile caseFile = factory.createCase("OPEN", "Desc", "CRITICAL", "Sector 4");
        
        assertNotNull(caseFile.getCaseId(), "Factory Pattern Violation: CaseId not generated.");
        assertEquals("OPEN", caseFile.getStatus());
    }
}
