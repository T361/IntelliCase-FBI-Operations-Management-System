package com.intellicase.domain;

/**
 * Domain entity for evidence records.
 * GRASP Information Expert: owns evidence attributes and exposes accessors.
 */
public class Evidence {
    private String evidenceId;
    private String caseId;
    private String status;
    private String custodian;
    private String integrityHash;
    private int sensitivityLevel;

    public Evidence() {
    }

    public Evidence(String evidenceId, String caseId, String status, String custodian,
                    String integrityHash, int sensitivityLevel) {
        this.evidenceId = evidenceId;
        this.caseId = caseId;
        this.status = status;
        this.custodian = custodian;
        this.integrityHash = integrityHash;
        this.sensitivityLevel = sensitivityLevel;
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(String evidenceId) {
        this.evidenceId = evidenceId;
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

    public String getCustodian() {
        return custodian;
    }

    public void setCustodian(String custodian) {
        this.custodian = custodian;
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    public void setIntegrityHash(String integrityHash) {
        this.integrityHash = integrityHash;
    }

    public int getSensitivityLevel() {
        return sensitivityLevel;
    }

    public void setSensitivityLevel(int sensitivityLevel) {
        this.sensitivityLevel = sensitivityLevel;
    }
}
