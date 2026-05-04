package com.intellicase.application;

/**
 * GoF Command Pattern — Command Interface.
 *
 * Encapsulates an audit action as an object, allowing it to be queued,
 * logged, or undone independently of the caller.
 *
 * GRASP Protected Variations: callers depend only on this interface,
 * not on specific audit implementations.
 */
public interface AuditCommand {

    /**
     * Execute the command.
     *
     * @return true if the command completed successfully.
     */
    boolean execute();

    /**
     * Human-readable description of what this command does,
     * used for audit trail display.
     */
    String describe();
}
