package com.intellicase.application;

import com.intellicase.domain.CaseFile;

/**
 * Observer that writes case creation events to stdout.
 * GoF Observer: concrete observer for case creation notifications.
 */
public class ConsoleNotificationObserver implements CaseNotificationObserver {
    @Override
    public void onCaseCreated(CaseFile caseFile) {
        if (caseFile != null) {
            System.out.println("[Notify] Case created: " + caseFile.getCaseId());
        }
    }
}
