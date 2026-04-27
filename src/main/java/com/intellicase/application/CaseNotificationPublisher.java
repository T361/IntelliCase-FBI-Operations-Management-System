package com.intellicase.application;

import java.util.ArrayList;
import java.util.List;

import com.intellicase.domain.CaseFile;

/**
 * GoF Observer publisher managing case creation observers.
 */
public class CaseNotificationPublisher {
    private final List<CaseNotificationObserver> observers = new ArrayList<>();

    public void addObserver(CaseNotificationObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    public void notifyCaseCreated(CaseFile caseFile) {
        for (CaseNotificationObserver observer : observers) {
            observer.onCaseCreated(caseFile);
        }
    }
}
