package com.intellicase.application;

import com.intellicase.domain.CaseFile;

/**
 * GoF Observer for case creation notifications.
 */
public interface CaseNotificationObserver {
    void onCaseCreated(CaseFile caseFile);
}
