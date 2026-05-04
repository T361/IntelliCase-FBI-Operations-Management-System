package com.intellicase.application;

import com.intellicase.dao.AuditLogDao;
import com.intellicase.domain.AuditLogEntry;

/**
 * GoF Command Pattern — Concrete Command.
 *
 * Encapsulates a single "write audit log entry" operation.
 * Caller creates the command, passes it to SecurityCommandInvoker;
 * the invoker calls execute() without knowing the implementation details.
 *
 * GRASP Creator: this class creates AuditLogEntry objects (owns the data).
 */
public class LogAuditEntryCommand implements AuditCommand {

    private final String action;
    private final String targetId;
    private final String actorId;
    private final AuditLogDao auditLogDao;

    public LogAuditEntryCommand(String action, String targetId,
                                String actorId, AuditLogDao auditLogDao) {
        this.action      = action;
        this.targetId    = targetId;
        this.actorId     = actorId;
        this.auditLogDao = auditLogDao;
    }

    @Override
    public boolean execute() {
        try {
            auditLogDao.create(new AuditLogEntry(action, targetId, actorId));
            return true;
        } catch (Exception ex) {
            System.err.println("[LogAuditEntryCommand] Failed: " + ex.getMessage());
            return false;
        }
    }

    @Override
    public String describe() {
        return "[AUDIT] action=" + action
            + " target=" + targetId
            + " actor=" + actorId;
    }
}
