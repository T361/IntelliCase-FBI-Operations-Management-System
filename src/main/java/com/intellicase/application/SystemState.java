package com.intellicase.application;

/**
 * GoF Singleton tracking global audit lockdown status.
 * GRASP Low Coupling: centralized state for write-guard decisions.
 */
public final class SystemState {
    private static SystemState instance;
    private boolean lockdownActive;

    private SystemState() {
        this.lockdownActive = false;
    }

    public static synchronized SystemState getInstance() {
        if (instance == null) {
            instance = new SystemState();
        }
        return instance;
    }

    public synchronized boolean isLockdownActive() {
        return lockdownActive;
    }

    public synchronized void activateLockdown() {
        lockdownActive = true;
    }

    public synchronized void deactivateLockdown() {
        lockdownActive = false;
    }
}
