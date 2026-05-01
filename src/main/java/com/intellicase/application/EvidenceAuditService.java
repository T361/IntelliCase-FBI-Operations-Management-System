package com.intellicase.application;

import java.util.List;

import com.intellicase.domain.Evidence;

/**
 * GoF Proxy interface for secure evidence inventory access (UC-14).
 */
public interface EvidenceAuditService {
    /**
     * Retrieve all inventory accessible to the given agent.
     */
    List<Evidence> getEvidenceInventory(String agentId);

    /**
     * Retrieve inventory filtered by Case ID.
     */
    List<Evidence> getEvidenceInventory(String agentId, String caseIdFilter);
}
