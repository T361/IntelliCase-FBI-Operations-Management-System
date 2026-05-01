# 🧩 Core Roles in IntelliCase FBI Operations Management System

IntelliCase implements a strict Role-Based Access Control (RBAC) architecture to ensure that sensitive federal data remains compartmentalized and secure. The system defines four primary roles with escalating privileges.

---

## 1. 👤 Public User (Civilian)
**Access Level:** Very Limited

This role acts as a public-facing civilian interface, similar to visitors on the official Federal Bureau of Investigation website.

*   **View public cases:** Access information on publicly disclosed cases (e.g., wanted persons, missing persons).
*   **Search/filter cases:** Ability to query the public database by keyword, location, or status.
*   **Submit tips or reports:** Send anonymous or contact-attached tips directly related to active cases.
*   **Restricted access:** Zero access to internal dashboards, security protocols, or evidence vaults.

---

## 2. 🕵️ Field Agent
**Access Level:** Operational

This is the “ground-level worker” role handling day-to-day investigative tasks in the field.

*   **View assigned cases only:** Compartmentalized access restricts viewing to cases where the agent is explicitly assigned.
*   **Update case status:** Can transition cases through operational phases (e.g., Under Investigation, Progress, Closed).
*   **Manage intelligence:** Upload new evidence, field reports, and operational notes.
*   **Update profiles:** Add suspect and witness information to the case dossier.
*   **Restricted access:** Cannot reassign cases to other agents or access system-wide analytics. Security and lockdown modules are hidden.

---

## 3. 🧑‍💼 Case Supervisor
**Access Level:** Management

The Case Supervisor acts as a departmental team leader overseeing multiple field agents and their active cases.

*   **Departmental oversight:** View all cases operating within their assigned department or jurisdiction.
*   **Delegation:** Assign and reassign cases to individual Field Agents based on workload and clearance.
*   **Approval workflows:** Approve or reject critical updates made by field agents.
*   **Case modification:** Modify high-level case details, priority levels, and classifications.
*   **Monitor progress:** Track chain-of-custody for evidence and overall case progression.
*   **Restricted access:** Cannot trigger facility-wide lockdowns or access cross-departmental analytics without Director authorization.

---

## 4. 🏢 FBI Director
**Access Level:** Full Control

This is the system administrator and strategic oversight role, representing the highest level of clearance.

*   **Global access:** View and manage all cases across all departments and field offices globally.
*   **Analytics dashboards:** View high-level analytics, including crime statistics, agent performance metrics, and system telemetry.
*   **User management:** Add, remove, or alter clearances for agents, supervisors, and other system personnel.
*   **Operation approval:** Final sign-off on high-level, highly sensitive, or black-ops operations.
*   **System-wide oversight:** Full authority to initiate facility lockdowns, verify immutable ledger integrity, and override system restrictions.
