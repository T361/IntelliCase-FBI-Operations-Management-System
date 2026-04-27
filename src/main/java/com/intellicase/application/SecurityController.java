package com.intellicase.application;

import com.intellicase.dao.AgentDao;
import com.intellicase.dao.AuditLogDao;
import com.intellicase.dao.ShadowProfileDao;
import com.intellicase.domain.Agent;
import com.intellicase.domain.AuditLogEntry;
import com.intellicase.domain.ShadowProfile;

/**
 * GRASP Controller implementing UC-07, UC-08, UC-09.
 * Handles shadow profiles, audit lockdown, and clearance promotions.
 */
public class SecurityController {
    private final ShadowProfileDao shadowProfileDao;
    private final AuditLogDao auditLogDao;
    private final AgentDao agentDao;
    private EncryptionStrategy encryptionStrategy;

    public SecurityController() {
        this.shadowProfileDao = new ShadowProfileDao();
        this.auditLogDao = new AuditLogDao();
        this.agentDao = new AgentDao();
        this.encryptionStrategy = new Aes256SimulationStrategy();
    }

    public void setEncryptionStrategy(EncryptionStrategy encryptionStrategy) {
        if (encryptionStrategy != null) {
            this.encryptionStrategy = encryptionStrategy;
        }
    }

    public boolean createShadowProfile(String profileId, String alias, String rawData, String caseId, String actorId) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[Security] Lockdown active; shadow profile creation blocked.");
            return false;
        }
        String encryptedData = encryptionStrategy.encrypt(rawData);
        ShadowProfile profile = new ShadowProfile(profileId, alias, encryptedData, caseId);
        shadowProfileDao.create(profile);
        auditLogDao.create(new AuditLogEntry("CREATE_SHADOW_PROFILE", profileId, actorId));
        return true;
    }

    public boolean activateAuditLockdown(String actorId) {
        if (SystemState.getInstance().isLockdownActive()) {
            return false;
        }
        auditLogDao.create(new AuditLogEntry("ACTIVATE_LOCKDOWN", "SYSTEM", actorId));
        SystemState.getInstance().activateLockdown();
        return true;
    }

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
}
