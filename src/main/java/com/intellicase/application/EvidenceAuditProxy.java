package com.intellicase.application;

import java.util.ArrayList;
import java.util.List;

import com.intellicase.dao.AgentDao;
import com.intellicase.dao.AuditLogDao;
import com.intellicase.domain.Agent;
import com.intellicase.domain.AuditLogEntry;
import com.intellicase.domain.Evidence;

/**
 * GoF Proxy enforcing clearance and integrity checks for UC-14.
 * Implements E2: restricted evidence masking for high-sensitivity items.
 */
public class EvidenceAuditProxy implements EvidenceAuditService {
    private static final int REQUIRED_CLEARANCE = 3;
    private static final int MAX_UNRESTRICTED_SENSITIVITY = 4;
    private static final String RESTRICTED_MASK = "[RESTRICTED — CLEARANCE INSUFFICIENT]";

    private final EvidenceAuditService target;
    private final AgentDao agentDao;
    private final AuditLogDao auditLogDao;

    public EvidenceAuditProxy(EvidenceAuditService target) {
        this.target = target;
        this.agentDao = new AgentDao();
        this.auditLogDao = new AuditLogDao();
    }

    @Override
    public List<Evidence> getEvidenceInventory(String agentId) {
        return getEvidenceInventory(agentId, null);
    }

    @Override
    public List<Evidence> getEvidenceInventory(String agentId, String caseIdFilter) {
        Agent agent = agentDao.findById(agentId);
        if (agent == null || agent.getClearanceLevel() < REQUIRED_CLEARANCE) {
            System.out.println("[EvidenceAudit] Clearance denied for agent: " + agentId);
            return new ArrayList<>();
        }

        // UC-14 Step 8: Log access event in immutable audit trail
        auditLogDao.create(new AuditLogEntry(
            "VIEW_EVIDENCE_INVENTORY", agentId, agentId));

        List<Evidence> raw = target.getEvidenceInventory(agentId, caseIdFilter);
        List<Evidence> processed = new ArrayList<>();

        for (Evidence ev : raw) {
            // E2: Mask restricted evidence if agent lacks top clearance
            if (ev.getSensitivityLevel() >= MAX_UNRESTRICTED_SENSITIVITY
                    && agent.getClearanceLevel() < 5) {
                processed.add(new Evidence(
                    ev.getEvidenceId(),
                    ev.getCaseId(),
                    ev.getStatus(),
                    RESTRICTED_MASK,
                    RESTRICTED_MASK,
                    ev.getSensitivityLevel()
                ));
            } else {
                processed.add(ev);
            }
        }
        return processed;
    }
}
