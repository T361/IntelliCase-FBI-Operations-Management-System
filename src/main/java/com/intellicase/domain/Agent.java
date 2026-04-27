package com.intellicase.domain;

/**
 * Domain entity for agent records.
 * GRASP Information Expert: encapsulates agent attributes.
 */
public class Agent {
    private String agentId;
    private String name;
    private int clearanceLevel;
    private int currentLoadScore;

    public Agent() {
    }

    public Agent(String agentId, String name, int clearanceLevel, int currentLoadScore) {
        this.agentId = agentId;
        this.name = name;
        this.clearanceLevel = clearanceLevel;
        this.currentLoadScore = currentLoadScore;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }

    public void setClearanceLevel(int clearanceLevel) {
        this.clearanceLevel = clearanceLevel;
    }

    public int getCurrentLoadScore() {
        return currentLoadScore;
    }

    public void setCurrentLoadScore(int currentLoadScore) {
        this.currentLoadScore = currentLoadScore;
    }
}
