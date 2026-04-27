package com.intellicase.application;

import com.intellicase.domain.Agent;

/**
 * Strategy that computes load as current score plus case weight.
 * GoF Strategy: default workload scoring policy.
 */
public class DefaultLoadScoreStrategy implements LoadScoreStrategy {
    private static final int CASE_WEIGHT = 10;

    @Override
    public int calculateLoadScore(Agent agent, int activeCaseCount) {
        if (agent == null) {
            return Integer.MAX_VALUE;
        }
        return agent.getCurrentLoadScore() + (activeCaseCount * CASE_WEIGHT);
    }
}
