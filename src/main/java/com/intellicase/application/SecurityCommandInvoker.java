package com.intellicase.application;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * GoF Command Pattern — Invoker.
 *
 * Decouples the caller (SecurityController, CaseController) from how
 * audit commands are executed and recorded.  Maintains a history deque
 * so that the last N commands can be inspected for the audit trail UI.
 *
 * GRASP Pure Fabrication: not mapped to a real-world concept; exists
 * purely to achieve Low Coupling between callers and the audit DAO.
 */
public final class SecurityCommandInvoker {

    private static final int MAX_HISTORY = 50;

    private static SecurityCommandInvoker instance;

    private final Deque<AuditCommand> history = new ArrayDeque<>();

    private SecurityCommandInvoker() { }

    public static synchronized SecurityCommandInvoker getInstance() {
        if (instance == null) {
            instance = new SecurityCommandInvoker();
        }
        return instance;
    }

    /**
     * Execute the given command and store it in history on success.
     *
     * @param cmd the command to run
     * @return true if execution succeeded
     */
    public boolean invoke(AuditCommand cmd) {
        System.out.println("[Invoker] " + cmd.describe());
        boolean ok = cmd.execute();
        if (ok) {
            history.addFirst(cmd);
            if (history.size() > MAX_HISTORY) {
                history.removeLast();
            }
        }
        return ok;
    }

    /** Return the most recent command in history, or null if empty. */
    public AuditCommand lastCommand() {
        return history.isEmpty() ? null : history.peekFirst();
    }

    /** Return history size for inspection in tests. */
    public int historySize() {
        return history.size();
    }
}
