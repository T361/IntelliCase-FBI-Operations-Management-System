package com.intellicase.domain;

/**
 * Domain entity for audit log entries.
 * GRASP Information Expert: retains audit event details.
 */
public class AuditLogEntry {
    private int logId;
    private String action;
    private String targetId;
    private String actorId;
    private String timestamp;

    public AuditLogEntry() {
    }

    public AuditLogEntry(String action, String targetId, String actorId) {
        this(0, action, targetId, actorId, null);
    }

    public AuditLogEntry(int logId, String action, String targetId, String actorId, String timestamp) {
        this.logId = logId;
        this.action = action;
        this.targetId = targetId;
        this.actorId = actorId;
        this.timestamp = timestamp;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
