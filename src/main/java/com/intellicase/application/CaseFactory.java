package com.intellicase.application;

import java.util.UUID;

import com.intellicase.domain.CaseFile;

/**
 * GoF Factory responsible for creating CaseFile instances.
 */
public class CaseFactory {
    public CaseFile createCase(String status, String description, String priority, String location) {
        String caseId = "CASE-" + UUID.randomUUID().toString();
        return new CaseFile(caseId, status, description, priority, location);
    }
}
