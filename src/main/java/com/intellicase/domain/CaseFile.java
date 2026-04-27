package com.intellicase.domain;

/**
 * Domain entity for a case record.
 * GRASP Information Expert: encapsulates case data and validation rules.
 */
public class CaseFile {
    private String caseId;
    private String status;
    private String description;
    private String priority;
    private String location;

    public CaseFile() {
    }

    public CaseFile(String caseId, String status, String description, String priority, String location) {
        this.caseId = caseId;
        this.status = status;
        this.description = description;
        this.priority = priority;
        this.location = location;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
