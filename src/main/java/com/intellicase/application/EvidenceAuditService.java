package com.intellicase.application;

import java.util.List;

import com.intellicase.domain.Evidence;

/**
 * GoF Proxy interface for secure evidence inventory access.
 */
public interface EvidenceAuditService {
    List<Evidence> getEvidenceInventory(String agentId);
}
