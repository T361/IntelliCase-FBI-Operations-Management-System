package com.intellicase.application;

import com.intellicase.domain.Agent;

/**
 * GoF Strategy for agent workload scoring.
 */
public interface LoadScoreStrategy {
    int calculateLoadScore(Agent agent, int activeCaseCount);
}
