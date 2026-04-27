package com.intellicase.application;

import java.util.ArrayList;
import java.util.List;

import com.intellicase.dao.AgentDao;
import com.intellicase.domain.Agent;
import com.intellicase.domain.Evidence;

/**
 * GoF Proxy enforcing clearance and integrity checks.
 */
public class EvidenceAuditProxy implements EvidenceAuditService {
    private static final int REQUIRED_CLEARANCE = 3;

    private final EvidenceAuditService target;
    private final AgentDao agentDao;

    public EvidenceAuditProxy(EvidenceAuditService target) {
        this.target = target;
        this.agentDao = new AgentDao();
    }

    @Override
    public List<Evidence> getEvidenceInventory(String agentId) {
        Agent agent = agentDao.findById(agentId);
        if (agent == null || agent.getClearanceLevel() < REQUIRED_CLEARANCE) {
            System.out.println("[EvidenceAudit] Clearance denied for agent: " + agentId);
            return new ArrayList<>();
        }
        List<Evidence> evidenceList = target.getEvidenceInventory(agentId);
        List<Evidence> verified = new ArrayList<>();
        for (Evidence evidence : evidenceList) {
            if (evidence.getIntegrityHash() != null && !evidence.getIntegrityHash().isBlank()) {
                verified.add(evidence);
            }
        }
        return verified;
    }
}
