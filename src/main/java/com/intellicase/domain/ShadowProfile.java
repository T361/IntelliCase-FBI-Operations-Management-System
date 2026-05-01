package com.intellicase.domain;

/**
 * Domain entity for shadow profiles.
 * GRASP Information Expert: encapsulates shadow profile data.
 */
public class ShadowProfile {
    private String profileId;
    private String alias;
    private String encryptedData;
    private String caseId;
    private String creatorAgentId;

    public ShadowProfile() {
    }

    public ShadowProfile(String profileId, String alias, String encryptedData, String caseId, String creatorAgentId) {
        this.profileId = profileId;
        this.alias = alias;
        this.encryptedData = encryptedData;
        this.caseId = caseId;
        this.creatorAgentId = creatorAgentId;
    }

    public ShadowProfile(String profileId, String alias, String encryptedData, String caseId) {
        this(profileId, alias, encryptedData, caseId, "UNKNOWN");
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getCreatorAgentId() {
        return creatorAgentId;
    }

    public void setCreatorAgentId(String creatorAgentId) {
        this.creatorAgentId = creatorAgentId;
    }
}
