package com.intellicase.application;

import java.util.List;

import com.intellicase.dao.EvidenceDao;
import com.intellicase.domain.Evidence;

/**
 * Core evidence audit service for UC-14 inventory retrieval.
 * GoF Proxy: real subject behind the secure audit proxy.
 */
public class EvidenceAuditServiceImpl implements EvidenceAuditService {
    private final EvidenceDao evidenceDao;

    public EvidenceAuditServiceImpl() {
        this.evidenceDao = new EvidenceDao();
    }

    @Override
    public List<Evidence> getEvidenceInventory(String agentId) {
        return evidenceDao.findAll();
    }
}
