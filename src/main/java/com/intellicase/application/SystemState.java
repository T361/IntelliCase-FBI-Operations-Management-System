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

    private String lockdownTimestamp;
    private String lockdownActorId;
    private boolean upsCheckPassed;

    public synchronized void activateLockdown(String actorId, String timestamp) {
        lockdownActive = true;
        lockdownActorId = actorId;
        lockdownTimestamp = timestamp;
    }

    public synchronized void activateLockdown() {
        activateLockdown("SYSTEM", java.time.Instant.now().toString());
    }

    public synchronized void deactivateLockdown() {
        lockdownActive = false;
        lockdownActorId = null;
        lockdownTimestamp = null;
    }

    public synchronized String getLockdownTimestamp() {
        return lockdownTimestamp;
    }

    public synchronized String getLockdownActorId() {
        return lockdownActorId;
    }

    public synchronized boolean isUpsCheckPassed() {
        return upsCheckPassed;
    }

    public synchronized void setUpsCheckPassed(boolean passed) {
        upsCheckPassed = passed;
    }
}
