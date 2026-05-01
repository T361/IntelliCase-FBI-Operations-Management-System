package com.intellicase.data;

import com.intellicase.dao.AgentDao;
import com.intellicase.dao.CaseFileDao;
import com.intellicase.dao.EvidenceDao;
import com.intellicase.domain.Agent;
import com.intellicase.domain.CaseFile;
import com.intellicase.domain.Evidence;

/**
 * Utility to seed the database with dummy data for UC-14 and other use cases.
 */
public class DataSeeder {
    public static void main(String[] args) {
        System.out.println("[DB] Seeding dummy data...");
        
        AgentDao agentDao = new AgentDao();
        CaseFileDao caseDao = new CaseFileDao();
        EvidenceDao evidenceDao = new EvidenceDao();

        // 1. Create Agents
        Agent highClearanceAgent = new Agent("AGT-7788-9900", "Special Agent OMEGA", 5, 10);
        Agent lowClearanceAgent = new Agent("AGT-1122-3344", "Probationary Agent ALPHA", 1, 5);
        
        agentDao.create(highClearanceAgent);
        agentDao.create(lowClearanceAgent);

        // 2. Create Cases
        CaseFile case1 = new CaseFile("CASE-2026-001", "ACTIVE", "Operation Nightfall - Cyber Espionage", "HIGH", "Washington, D.C.");
        CaseFile case2 = new CaseFile("CASE-2026-002", "OPEN", "Blue Diamond Heist", "MEDIUM", "New York City, NY");
        
        caseDao.create(case1);
        caseDao.create(case2);

        // 3. Create Evidence
        String h1 = "sha256:e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        Evidence e1 = new Evidence("EVD-001", "CASE-2026-001", "SECURED", "AGT-7788-9900", h1, 4);
        
        String h2 = "sha256:5994471abb01112afcc18159f6cc74b4f511b99806da59b3caf5a9c173cacfc5";
        Evidence e2 = new Evidence("EVD-002", "CASE-2026-001", "ANALYSIS", "AGT-7788-9900", h2, 3);
        
        String h3 = "sha256:855c14ca495991b7852b855e3b0c44298fc1c149afbf4c8996fb92427ae41e464";
        Evidence e3 = new Evidence("EVD-003", "CASE-2026-002", "SECURED", "AGT-1122-3344", h3, 2);
        
        evidenceDao.create(e1);
        evidenceDao.create(e2);
        evidenceDao.create(e3);

        System.out.println("[DB] Seeding complete.");
        DatabaseConnection.getInstance().close();
    }
}
