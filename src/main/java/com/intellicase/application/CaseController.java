package com.intellicase.application;

import java.util.List;

import com.intellicase.dao.AuditLogDao;
import com.intellicase.dao.CaseFileDao;
import com.intellicase.domain.AuditLogEntry;
import com.intellicase.domain.CaseFile;
import com.intellicase.domain.Evidence;

/**
 * GRASP Controller implementing UC-11, UC-14, UC-15.
 */
public class CaseController {
    private static final String OVERRIDE_CODE = "OVERRIDE-ALPHA";

    private final CaseFileDao caseFileDao;
    private final AuditLogDao auditLogDao;
    private final CaseFactory caseFactory;
    private final CaseNotificationPublisher publisher;
    private final EvidenceAuditService evidenceAuditService;

    public CaseController() {
        this.caseFileDao = new CaseFileDao();
        this.auditLogDao = new AuditLogDao();
        this.caseFactory = new CaseFactory();
        this.publisher = new CaseNotificationPublisher();
        this.publisher.addObserver(new ConsoleNotificationObserver());
        this.evidenceAuditService = new EvidenceAuditProxy(new EvidenceAuditServiceImpl());
    }

    public void addObserver(CaseNotificationObserver observer) {
        publisher.addObserver(observer);
    }

    /**
     * UC-11: Create Smart Case Initializer.
     */
    public CaseFile createSmartCase(String status, String description, String priority, String location, String actorId) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[Case] Lockdown active; case creation blocked.");
            return null;
        }
        CaseFile caseFile = caseFactory.createCase(status, description, priority, location);
        caseFileDao.create(caseFile);
        auditLogDao.create(new AuditLogEntry("CREATE_CASE", caseFile.getCaseId(), actorId));
        publisher.notifyCaseCreated(caseFile);
        return caseFile;
    }

    /**
     * UC-14: Secure Evidence Inventory Audit.
     */
    public List<Evidence> secureEvidenceAudit(String agentId) {
        return evidenceAuditService.getEvidenceInventory(agentId);
    }

    /**
     * UC-15: Deactivate System Audit Lockdown.
     */
    public boolean deactivateAuditLockdown(String overrideCode, String actorId) {
        if (!OVERRIDE_CODE.equals(overrideCode)) {
            System.out.println("[Case] Override denied.");
            return false;
        }
        SystemState.getInstance().deactivateLockdown();
        auditLogDao.create(new AuditLogEntry("DEACTIVATE_LOCKDOWN", "SYSTEM", actorId));
        return true;
    }
}
