package com.intellicase.application;

import java.util.List;

import com.intellicase.dao.AgentDao;
import com.intellicase.dao.AuditLogDao;
import com.intellicase.dao.EvidenceDao;
import com.intellicase.domain.Agent;
import com.intellicase.domain.AuditLogEntry;
import com.intellicase.domain.Evidence;

/**
 * GRASP Controller implementing UC-01, UC-03, UC-05.
 */
public class EvidenceController {
    private static final int LOAD_THRESHOLD = 100;

    private final EvidenceDao evidenceDao;
    private final AuditLogDao auditLogDao;
    private final AgentDao agentDao;
    private LoadScoreStrategy loadScoreStrategy;

    public EvidenceController() {
        this.evidenceDao = new EvidenceDao();
        this.auditLogDao = new AuditLogDao();
        this.agentDao = new AgentDao();
        this.loadScoreStrategy = new DefaultLoadScoreStrategy();
    }

    public void setLoadScoreStrategy(LoadScoreStrategy loadScoreStrategy) {
        if (loadScoreStrategy != null) {
            this.loadScoreStrategy = loadScoreStrategy;
        }
    }

    /**
     * UC-01: Initiate Digital Handshake.
     */
    public boolean initiateDigitalHandshake(String evidenceId, String actorId) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[Evidence] Lockdown active; handshake blocked.");
            return false;
        }
        Evidence evidence = evidenceDao.findById(evidenceId);
        if (evidence == null) {
            System.out.println("[Evidence] Evidence not found: " + evidenceId);
            return false;
        }
        evidenceDao.updateStatus(evidenceId, "IN_TRANSIT");
        auditLogDao.create(new AuditLogEntry("DIGITAL_HANDSHAKE", evidenceId, actorId));
        return true;
    }

    /**
     * UC-05: Assign Agent via Smart Load Score.
     */
    public boolean assignAgentSmartLoad(String agentId, int activeCaseCount, String actorId) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[Evidence] Lockdown active; agent assignment blocked.");
            return false;
        }
        Agent agent = agentDao.findById(agentId);
        if (agent == null) {
            System.out.println("[Evidence] Agent not found: " + agentId);
            return false;
        }
        int newLoad = loadScoreStrategy.calculateLoadScore(agent, activeCaseCount);
        if (newLoad > LOAD_THRESHOLD) {
            System.out.println("[Evidence] Agent load exceeds threshold: " + newLoad);
            return false;
        }
        agentDao.updateLoadScore(agentId, newLoad);
        auditLogDao.create(new AuditLogEntry("ASSIGN_AGENT", agentId, actorId));
        return true;
    }

    /**
     * UC-03: View Immutable Audit Trail.
     */
    public List<AuditLogEntry> viewAuditTrail(String targetId) {
        return auditLogDao.findByTargetId(targetId);
    }
}
