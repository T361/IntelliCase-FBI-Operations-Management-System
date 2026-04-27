# IntelliCase: FBI Operations Management System 🦅

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue?style=for-the-badge&logo=javafx)
![SQLite](https://img.shields.io/badge/SQLite-Database-lightgrey?style=for-the-badge&logo=sqlite)
![Architecture](https://img.shields.io/badge/Architecture-Layered%20%7C%20SPA-brightgreen?style=for-the-badge)

IntelliCase is a highly secure, data-dense Operations Management System architected specifically for federal law enforcement. Built as a pure Java monolith with a zero-configuration SQLite backend, it bridges the gap between field operations, evidence custody, and high-level administrative oversight.

---

## 📖 Table of Contents
1. [Project Overview](#-project-overview)
2. [Advanced UI/UX Architecture](#-advanced-uiux-architecture)
3. [Software Design Patterns](#-software-design-patterns)
4. [Use Case Execution Matrix](#-use-case-execution-matrix)
5. [Setup & Installation](#-setup--installation)
6. [Team Architecture](#-team-architecture)

---

## 🔍 Project Overview

The current federal landscape relies on disjointed systems, leading to workload burnout, stalled investigations, and vulnerable evidence chains. IntelliCase unifies these workflows through a secure, offline-capable desktop application. 

**Key Capabilities:**
* **Cryptographic Evidence Tracking:** Mandated digital handshakes for physical evidence transfers.
* **Smart Workload Balancing:** Algorithmic assignment of agents to prevent burnout.
* **Absolute Audit Integrity:** Immutable, append-only ledgers for every system action.
* **Shadow Profiling:** AES-256 simulated encryption for high-risk confidential informants.

---

## 🌌 Advanced UI/UX Architecture

IntelliCase breaks away from traditional, static desktop forms by implementing a modern, highly interactive **Cyberpunk/Palantir-inspired aesthetic** using pure JavaFX components (No external UI libraries).

* **SPA (Single Page Application) Routing:** The UI utilizes a custom `ViewRouter` Singleton. Instead of opening multiple windows, the central workspace seamlessly swaps FXML nodes (Dashboards, Vaults, Consoles) within a persistent `StackPane`, maintaining background context.
* **Hardware-Accelerated Particle Engine:** A custom `ParticleCanvas` runs via JavaFX `AnimationTimer`. It continuously renders a drifting, starry data-stream in the deep background, providing a dynamic, high-tech atmosphere without dropping frame rates.
* **Sensory Audio Feedback:** Integrated `AudioClip` managers provide immediate, subtle auditory responses (blips, clicks, error buzzes) to all physical mouse interactions, heightening user immersion.
* **Sequential Overlay Guidance:** A built-in `GuidanceOverlayManager` uses pulsing CSS shadows and directional tooltips to lead users exactly through complex, multi-step administrative workflows.

---

## 📐 Software Design Patterns

The backend is strictly segregated from the Presentation layer and extensively utilizes industry-standard design principles.

### GRASP (General Responsibility Assignment Software Patterns)
* **Controller:** UI interactions are intercepted by specific facade controllers (`SecurityController`, `CaseController`) which delegate to domain objects.
* **Creator:** Complex entities like `Evidence` and `ShadowProfile` manage their own instantiation and validation logic before database insertion.
* **Information Expert:** Calculation logic (like Load Scoring) is assigned directly to the classes that hold the relevant integer data.
* **High Cohesion / Low Coupling:** Strict Layered Architecture ensures the UI knows nothing about `java.sql.*`, and the DAOs know nothing about `javafx.scene.*`.

### GoF (Gang of Four) Patterns
* **Singleton:** Utilized for the `DatabaseConnection` and immutable `AuditLog` managers to prevent resource leaks and guarantee a single source of truth.
* **Strategy:** Applied to algorithms such as the `LoadScoreStrategy` and `EncryptionStrategy`, allowing logic to be swapped dynamically at runtime without altering core objects.
* **Factory:** Utilized for generating unique identifiers and assembling `CaseFile` entities.
* **Observer / Proxy:** Implemented for triggering asynchronous UI updates when database states change, and acting as gatekeepers for restricted evidence audits.

---

## 🚀 Use Case Execution Matrix

This system implements 9 core operations, architected and deployed by the respective engineering team members:

### Security Clearance & Overrides 
* **UC-07: Create Shadow Profile:** Generates encrypted, restricted-access informant data.
* **UC-08: Trigger Audit Lockdown:** Flips global system states to read-only during external audits.
* **UC-09: Promote Security Clearance:** Manages Agent RBAC (Role-Based Access Control) integer levels.

### Evidence Logic & Algorithms 
* **UC-01: Initiate Digital Handshake:** Secures evidence status to "In Transit".
* **UC-05: Smart Load Score Assignment:** Calculates current workload prior to assigning task forces.
* **UC-03: View Immutable Audit Trail:** Retrieves restricted, append-only historical ledgers.

### Automation & Integrity 
* **UC-11: Create Smart Case:** Initializes new investigations with factory-generated hashing.
* **UC-15: Deactivate System Audit Lockdown:** Restores global read/write access via Director override.
* **UC-14: Secure Evidence Inventory Audit:** Validates real-time digital integrity hashes for physical evidence.

---

## ⚙️ Setup & Installation

IntelliCase requires **zero** external server configuration. The SQLite database is generated dynamically upon the first run.

### Prerequisites
* JDK 17 or higher
* Maven 3.8+

### Build and Run
1. Clone the repository:
   ```bash
   git clone https://github.com/T361/IntelliCase-FBI-Operations-Management-System.git
   cd IntelliCase-FBI-Operations-Management-System
   ```
2. Compile and launch:
   ```bash
   mvn clean javafx:run
   ```

---

## 👥 Team Architecture

| Role | Member | Responsibilities |
|------|--------|------------------|
| **Security Lead** | i243166 | UC-07, UC-08, UC-09 |
| **Evidence Lead** | i243158 | UC-01, UC-05, UC-03 |
| **Automation Lead** | i243015 | UC-11, UC-15, UC-14 |
