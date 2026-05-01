package com.intellicase.application;

import java.time.Instant;

import com.intellicase.dao.AgentDao;
import com.intellicase.dao.AuditLogDao;
import com.intellicase.dao.CaseFileDao;
import com.intellicase.dao.ShadowProfileDao;
import com.intellicase.domain.Agent;
import com.intellicase.domain.AuditLogEntry;
import com.intellicase.domain.ShadowProfile;

/**
 * GRASP Controller implementing UC-07, UC-08, UC-09.
 * UC-08 includes: UPS check, credential verification, global lockdown cascade,
 * and DOJ notification generation.
 */
public class SecurityController {

    // UC-08: Director credential constants (simulated biometric/password)
    public static final String DIRECTOR_ID = "DIR-0001";
    public static final String DIRECTOR_PASSWORD = "BLACKSITE-ALPHA";
    private static final String OVERRIDE_CODE = "NCIS-X-99";

    private final ShadowProfileDao shadowProfileDao;
    private final AuditLogDao auditLogDao;
    private final AgentDao agentDao;
    private final CaseFileDao caseFileDao;
    private EncryptionStrategy encryptionStrategy;

    public SecurityController() {
        this.shadowProfileDao = new ShadowProfileDao();
        this.auditLogDao = new AuditLogDao();
        this.agentDao = new AgentDao();
        this.caseFileDao = new CaseFileDao();
        this.encryptionStrategy = new Aes256SimulationStrategy();
    }

    public void setEncryptionStrategy(EncryptionStrategy strategy) {
        if (strategy != null) {
            this.encryptionStrategy = strategy;
        }
    }

    // ── UC-07: Create Shadow Profile ─────────────────────────────────────────

    public enum ShadowProfileResult {
        SUCCESS, ALIAS_IN_USE, PROFILE_EXISTS, CASE_NOT_FOUND, LOCKDOWN_ACTIVE
    }

    public ShadowProfileResult createShadowProfile(
            String profileId, String alias, String rawData, String caseId, String actorId) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[Security] Lockdown active; shadow profile creation blocked.");
            return ShadowProfileResult.LOCKDOWN_ACTIVE;
        }

        // Case ID validation (Must exist in database)
        if (caseFileDao.findById(caseId) == null) {
            return ShadowProfileResult.CASE_NOT_FOUND;
        }

        // Profile ID uniqueness
        if (shadowProfileDao.findById(profileId) != null) {
            return ShadowProfileResult.PROFILE_EXISTS;
        }

        // E2: Alias already in use
        if (shadowProfileDao.findByAlias(alias) != null) {
            return ShadowProfileResult.ALIAS_IN_USE;
        }

        String encryptedData = encryptionStrategy.encrypt(rawData);
        // Save with creator agent ID to restrict visibility
        ShadowProfile profile = new ShadowProfile(profileId, alias, encryptedData, caseId, actorId);
        shadowProfileDao.create(profile);
        auditLogDao.create(new AuditLogEntry("CREATE_SHADOW_PROFILE", profileId, actorId));
        return ShadowProfileResult.SUCCESS;
    }

    // E1: Unauthorized Agent attempts view
    public String viewShadowProfile(String profileId, String requestingAgentId) {
        ShadowProfile profile = shadowProfileDao.findById(profileId);
        if (profile == null) {
            return "Profile not found.";
        }
        
        // Visibility restricted to creator and FBI Director
        if (!requestingAgentId.equals(profile.getCreatorAgentId()) && !DIRECTOR_ID.equals(requestingAgentId)) {
            auditLogDao.create(new AuditLogEntry("UNAUTHORIZED_PROFILE_VIEW", profileId, requestingAgentId));
            return "ACCESS DENIED: Unauthorized view attempt logged.";
        }

        // Simulate decryption for authorized users (MFA passed)
        // Since we are using Aes256SimulationStrategy which just prefixes, we can't truly decrypt, 
        // but we'll return a simulated decrypted string for the use case.
        return "Decrypted Details: " + profile.getEncryptedData().replace("AES-256-SIM{", "").replace("}", "");
    }

    // E3: Informant identity verification fails prevents linking to financial payout account
    public boolean linkFinancials(String profileId, boolean identityVerified) {
        if (!identityVerified) {
            System.out.println("[Security] E3: Informant identity verification failed. Financial link blocked.");
            return false;
        }
        System.out.println("[Security] Profile " + profileId + " successfully linked to secure payout account.");
        return true;
    }

    // ── UC-08: Activate System-Wide Audit Lockdown ───────────────────────────

    /**
     * Simulates UPS dependency check (Special Requirement 2).
     * Returns true if UPS is operational.
     */
    public boolean performUpsCheck() {
        // Simulate UPS check — always passes in this environment
        SystemState.getInstance().setUpsCheckPassed(true);
        System.out.println("[UC-08] UPS dependency check: PASSED");
        return true;
    }

    public LockdownAuthResult authenticateDirector(String username, String password) {
        // E3: Simulate expired password scenario (keep for extension demonstration)
        if ("EXPIRED".equals(password)) {
            return LockdownAuthResult.PASSWORD_EXPIRED;
        }

        // Authenticate against database
        com.intellicase.dao.AppUserDao userDao = new com.intellicase.dao.AppUserDao();
        com.intellicase.domain.AppUser user = userDao.authenticate(username, password);

        if (user == null) {
            auditLogDao.create(new AuditLogEntry(
                "LOCKDOWN_AUTH_FAILED", username, username));
            return LockdownAuthResult.CREDENTIAL_FAILED; // Or IDENTITY_FAILED
        }
        
        if (!"FBI_DIRECTOR".equals(user.getRole())) {
            auditLogDao.create(new AuditLogEntry(
                "LOCKDOWN_AUTH_FAILED_NOT_DIRECTOR", username, username));
            return LockdownAuthResult.IDENTITY_FAILED;
        }

        return LockdownAuthResult.SUCCESS;
    }

    /**
     * Activates the system-wide audit lockdown (UC-08 Main Flow).
     * Requires prior performUpsCheck() and authenticateDirector() calls.
     */
    public boolean activateAuditLockdown(String actorId) {
        if (SystemState.getInstance().isLockdownActive()) {
            return false;
        }
        String timestamp = Instant.now().toString();
        SystemState.getInstance().activateLockdown(actorId, timestamp);

        // Special Requirement 3: Log DOJ notification with exact timestamp
        auditLogDao.create(new AuditLogEntry(
            "ACTIVATE_LOCKDOWN", "SYSTEM", actorId));
        auditLogDao.create(new AuditLogEntry(
            "DOJ_NOTIFICATION_SENT", "DOJ-FREEZE-" + timestamp, actorId));

        System.out.println("[UC-08] LOCKDOWN ACTIVATED at " + timestamp);
        System.out.println("[UC-08] DOJ notification dispatched.");
        return true;
    }

    /**
     * Validates the override code for lockdown deactivation (UC-15).
     */
    public boolean isValidOverrideCode(String code) {
        return OVERRIDE_CODE.equals(code);
    }

    // ── UC-09: Promote Security Clearance ────────────────────────────────────

    public boolean promoteSecurityClearance(String agentId, String actorId) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[Security] Lockdown active; clearance promotion blocked.");
            return false;
        }
        Agent agent = agentDao.findById(agentId);
        if (agent == null) {
            System.out.println("[Security] Agent not found: " + agentId);
            return false;
        }
        int newLevel = agent.getClearanceLevel() + 1;
        agentDao.updateClearance(agentId, newLevel);
        auditLogDao.create(new AuditLogEntry("PROMOTE_CLEARANCE", agentId, actorId));
        return true;
    }

    // ── Result Enum for UC-08 Auth ────────────────────────────────────────────

    /**
     * UC-08 authentication result codes including extension scenarios.
     */
    public enum LockdownAuthResult {
        SUCCESS,
        IDENTITY_FAILED,    // E1: biometric/ID check failed
        CREDENTIAL_FAILED,  // E1: password wrong
        PASSWORD_EXPIRED    // E3: director's password has expired
    }
}
