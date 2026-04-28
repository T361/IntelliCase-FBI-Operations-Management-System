# 🦅 IntelliCase: Federal Bureau of Investigation Operations System

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX_21-UI_Engine-2c6396?style=for-the-badge&logo=java&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-Offline_DB-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Layered%20SPA-00E5FF?style=for-the-badge)
![License](https://img.shields.io/badge/License-Academic-lightgrey?style=for-the-badge)

> *"Justice through Efficiency, Security through Architecture."*

---

## 📑 Table of Contents

1.  [Executive Summary](#-executive-summary)
2.  [Core Innovations & UI/UX](#-core-innovations--uiux)
3.  [System Architecture (GRASP & GoF)](#-system-architecture-grasp--gof)
4.  [4-Tier Layered Architecture](#-4-tier-layered-architecture)
5.  [Project Directory Structure](#-project-directory-structure)
6.  [Module Deep Dive](#-module-deep-dive)
7.  [Use Case Matrix & Team Division](#-use-case-matrix--team-division)
8.  [Detailed Use Case Specifications](#-detailed-use-case-specifications)
9.  [Database Schema](#-database-schema)
10. [Security Model](#-security-model)
11. [Testing & Quality Assurance](#-testing--quality-assurance)
12. [Installation & Deployment](#-installation--deployment)
13. [Configuration & Build](#-configuration--build)
14. [Screenshots & Visual Design](#-screenshots--visual-design)
15. [Troubleshooting](#-troubleshooting)
16. [Team Architecture](#-team-architecture)
17. [Academic Compliance](#-academic-compliance)

---

## 🚀 Executive Summary

The current federal law enforcement landscape relies on fragmented, siloed systems that lead to workload burnout, stalled investigations, compromised evidence chains, and critical security oversights. **IntelliCase** is a production-grade, highly secure, offline-first desktop environment architected specifically for federal operations management.

Built entirely on a pure **Java SE 17** foundation with **JavaFX 21** for the presentation layer and **SQLite** for zero-configuration persistent storage, IntelliCase eradicates operational inefficiency and hardens evidence chain-of-custody through:

- **Advanced cryptographic simulation** (AES-256 encryption strategy pattern)
- **Immutable, append-only audit logs** with tamper-evident integrity hashing
- **Algorithmic workload balancing** to prevent agent burnout via smart load scoring
- **Role-Based Access Control (RBAC)** with integer clearance level promotion
- **System-wide audit lockdown** capability for external compliance reviews

The system processes 9 core federal operations spanning evidence tracking, security overrides, case management, and automated auditing — all within a stunning, Cyberpunk/Palantir-inspired dark-mode UI that features real-time particle rendering, synthesized audio feedback, and sequential guided onboarding overlays.

---

## 🌌 Core Innovations & UI/UX

IntelliCase breaks away from traditional, static desktop forms by implementing a modern, highly interactive **Cyberpunk/Palantir-inspired aesthetic** using pure JavaFX components. No external UI libraries are used — every visual effect is hand-built.

### Single Page Application (SPA) Routing

The UI employs a custom **`ViewRouter` Singleton** that dynamically hot-swaps FXML scene graph nodes within a persistent `StackPane` layout. This eliminates the multi-window anti-pattern entirely:

- **Zero-latency navigation** — no window creation/destruction overhead
- **Persistent layout context** — sidebar, topbar, and particle engine persist across routes
- **Cache-friendly** — previously loaded FXML nodes can be retained in memory
- **Routes:** `MainMenu.fxml` → `SecurityConsole.fxml` → `EvidenceVault.fxml` → `CaseDashboard.fxml`

### Hyper-Particle Fluid Engine

A highly optimized **`HyperParticleEngine`** (extending `Canvas`) runs via a 60fps `AnimationTimer`, rendering hundreds of mathematically-driven, floating data-stream particles in the deep background:

- **Hardware-accelerated** — direct `GraphicsContext2D` rendering bypasses JavaFX node overhead
- **Responsive** — canvas dimensions bind to `rootStack` width/height properties
- **Non-intrusive** — `setMouseTransparent(true)` ensures particles never intercept user clicks
- **Aesthetic** — creates a living, breathing "data flow" atmosphere behind all UI panels

### Sensory Synthesizer Engine

A crash-proof **`AudioFeedbackManager`** uses raw JVM `javax.sound.sampled` byte-array generation to provide immediate auditory feedback on every physical interaction:

- **Hover Blip:** 1200 Hz sine wave, 10ms duration — rapid high-pitch acknowledgment
- **Click Tone:** 200 Hz square wave → 1000 Hz sine composite, 30ms — deep punchy confirmation
- **Error Buzz:** 160 Hz sine wave, 120ms — low rumble alert
- **Technical specs:** 44.1 kHz sample rate, 16-bit signed PCM, mono channel
- **Crash immunity:** All `Exception` and `UnsatisfiedLinkError` are silently swallowed, preventing Linux PipeWire/PulseAudio segfaults (Exit 137)
- **Daemon threads:** Audio threads are marked as daemon to prevent JVM hang on application exit
- **Global binding:** Recursive scene-graph traversal via `bindToScene(Scene)` attaches listeners to every `Button`, `TextField`, and `TextArea` in the entire application

### Guided Flow Overlays

A built-in **`HUDGuidanceOverlay`** system implements step-by-step contextual guidance using pulsing CSS neon borders (`#00f3ff` / `#ffe66d`) and directional tooltips:

- Activates on first navigation to guide new users
- Highlights the next actionable element with a glowing border
- Provides contextual instruction text positioned near the target
- Auto-clears when the user navigates away from the guided context

---

## 🏗 System Architecture (GRASP & GoF)

The system rigorously enforces industry-standard software design principles across all layers.

### GRASP Principles (General Responsibility Assignment Software Patterns)

| Principle | Implementation | Classes |
| :--- | :--- | :--- |
| **Controller** | UI events are intercepted by facade controllers that delegate to domain objects. Presentation controllers (`SecurityConsoleController`, `EvidenceVaultController`) handle FXML events and call application-layer controllers. | `SecurityController`, `CaseController`, `EvidenceController` |
| **Creator** | Complex entities manage their own instantiation logic. The `CaseFactory` handles multi-step UUID generation and object assembly. | `CaseFactory`, `ShadowProfile` |
| **Information Expert** | Calculation logic is assigned to the classes holding the relevant data. `LoadScoreStrategy` executes workload math directly on `Agent` objects it evaluates. | `DefaultLoadScoreStrategy`, `Agent` |
| **High Cohesion** | Each class has a single, focused responsibility. DAOs only handle SQL. Controllers only handle event routing. Domain objects only hold state. | All classes |
| **Low Coupling** | Strict layered architecture ensures the UI knows nothing about `java.sql.*`, and the DAOs know nothing about `javafx.scene.*`. | Architecture-wide |
| **Polymorphism** | Strategy interfaces allow runtime swapping of algorithm implementations without altering core objects. | `LoadScoreStrategy`, `EncryptionStrategy` |
| **Indirection** | Application controllers act as intermediaries between presentation and data layers, preventing direct coupling. | `SecurityController`, `CaseController` |

### Gang of Four (GoF) Design Patterns

| Pattern | Type | Implementation | Key Class(es) |
| :--- | :--- | :--- | :--- |
| **Singleton** | Creational | Ensures exactly one instance with private constructor and `getInstance()`. | `DatabaseConnection`, `SystemState`, `ViewRouter` |
| **Strategy** | Behavioral | Defines interchangeable algorithm families behind common interfaces. | `LoadScoreStrategy` → `DefaultLoadScoreStrategy`, `EncryptionStrategy` → `Aes256SimulationStrategy` |
| **Factory** | Creational | Handles complex object construction with auto-generated UUIDs. | `CaseFactory` |
| **Observer** | Behavioral | Asynchronous UI notification relays when database states change. | `CaseNotificationPublisher` |
| **Proxy** | Structural | Acts as gatekeeper for restricted evidence audit access. | Audit access control layer |

---

## 🧱 4-Tier Layered Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                         │
│  JavaFX SPA Router │ FXML Views │ CSS Theme │ Audio Engine  │
│  RootLayoutController │ ViewRouter │ ParticleEngine         │
├─────────────────────────────────────────────────────────────┤
│                   APPLICATION LAYER                          │
│  SecurityController │ CaseController │ EvidenceController   │
│  CaseFactory │ SystemState │ Strategy Interfaces             │
├─────────────────────────────────────────────────────────────┤
│                     DOMAIN LAYER                             │
│  CaseFile │ Evidence │ Agent │ ShadowProfile │ AuditLogEntry│
├─────────────────────────────────────────────────────────────┤
│                   DATA ACCESS LAYER                          │
│  AgentDao │ CaseDao │ EvidenceDao │ AuditLogDao             │
│  ShadowProfileDao │ DatabaseConnection (Singleton)          │
└─────────────────────────────────────────────────────────────┘
                           ▼
                  ┌─────────────────┐
                  │   SQLite (JDBC) │
                  │  intellicase.db │
                  └─────────────────┘
```

**Strict boundary rules:**
- Presentation → Application (only via public controller methods)
- Application → Domain (creates, validates, transforms domain objects)
- Application → Data Access (via DAO interfaces for CRUD)
- Data Access → SQLite (raw JDBC with PreparedStatements)
- **No layer skipping** — Presentation NEVER touches DAOs or SQL directly

---

## 📂 Project Directory Structure

```text
IntelliCase-FBI-Operations-Management-System/
├── src/
│   ├── main/
│   │   ├── java/com/intellicase/
│   │   │   ├── application/                    # Business Logic Layer
│   │   │   │   ├── CaseController.java         #   UC-11, UC-14, UC-15 facade
│   │   │   │   ├── CaseFactory.java            #   GoF Factory for CaseFile creation
│   │   │   │   ├── CaseNotificationPublisher.java # GoF Observer for async UI updates
│   │   │   │   ├── EvidenceController.java     #   UC-01, UC-03, UC-05 facade
│   │   │   │   ├── SecurityController.java     #   UC-07, UC-08, UC-09 facade
│   │   │   │   ├── SystemState.java            #   GoF Singleton global lockdown state
│   │   │   │   ├── Aes256SimulationStrategy.java # GoF Strategy for encryption
│   │   │   │   ├── DefaultLoadScoreStrategy.java # GoF Strategy for load scoring
│   │   │   │   ├── EncryptionStrategy.java     #   Strategy interface
│   │   │   │   └── LoadScoreStrategy.java      #   Strategy interface
│   │   │   │
│   │   │   ├── dao/                            # Data Access Objects Layer
│   │   │   │   ├── AgentDao.java               #   CRUD for Agent entities
│   │   │   │   ├── AuditLogDao.java            #   Append-only audit log access
│   │   │   │   ├── CaseDao.java                #   CRUD for CaseFile entities
│   │   │   │   ├── EvidenceDao.java            #   CRUD for Evidence entities
│   │   │   │   └── ShadowProfileDao.java       #   CRUD for encrypted profiles
│   │   │   │
│   │   │   ├── data/                           # Database Infrastructure
│   │   │   │   └── DatabaseConnection.java     #   GoF Singleton JDBC manager
│   │   │   │
│   │   │   ├── domain/                         # Domain Model (POJOs)
│   │   │   │   ├── Agent.java                  #   Field agent entity
│   │   │   │   ├── AuditLogEntry.java          #   Immutable audit record
│   │   │   │   ├── CaseFile.java               #   Investigation case entity
│   │   │   │   ├── Evidence.java               #   Physical evidence entity
│   │   │   │   └── ShadowProfile.java          #   Encrypted informant profile
│   │   │   │
│   │   │   └── presentation/                   # JavaFX Presentation Layer
│   │   │       ├── MainApp.java                #   Application entry point
│   │   │       ├── ViewRouter.java             #   GoF Singleton SPA router
│   │   │       ├── AudioFeedbackManager.java   #   javax.sound.sampled synthesizer
│   │   │       ├── GuidanceOverlayManager.java #   Sequential overlay controller
│   │   │       ├── ParticleCanvas.java         #   Legacy particle canvas
│   │   │       ├── RootLayoutController.java   #   Root layout + sidebar nav
│   │   │       ├── MainMenuController.java     #   Main menu card grid
│   │   │       ├── DashboardController.java    #   Metrics dashboard
│   │   │       ├── SecurityConsoleController.java # UC-07/08/09/15 UI
│   │   │       ├── EvidenceVaultController.java   # UC-01/03/05 UI
│   │   │       ├── CaseDashboardController.java   # UC-11/14 UI
│   │   │       └── effects/                    #   Visual Effects Sub-package
│   │   │           ├── HyperParticleEngine.java#     60fps particle renderer
│   │   │           ├── AnimationUtils.java     #     Floating/breathing animations
│   │   │           └── HUDGuidanceOverlay.java #     Neon guidance highlights
│   │   │
│   │   └── resources/ui/                       # FXML & CSS Resources
│   │       ├── CyberpunkUI.css                 #   Complete dark theme stylesheet
│   │       ├── RootLayout.fxml                 #   Root SPA container layout
│   │       ├── MainMenu.fxml                   #   Navigation card grid
│   │       ├── SecurityConsole.fxml            #   Security operations forms
│   │       ├── EvidenceVault.fxml              #   Evidence management forms
│   │       └── CaseDashboard.fxml              #   Case operations dashboard
│   │
│   └── test/java/com/intellicase/             # JUnit 5 Test Suite
│       ├── DatabaseConnectionTest.java         #   Singleton & JDBC validation
│       └── IntelliCaseBackendTest.java         #   Full backend QA (OOP, BL, DB, GoF)
│
├── pom.xml                                     # Maven POM (Java 17, JavaFX 21, SQLite)
├── checkstyle.xml                              # Code style enforcement (140 char, no *)
├── intellicase.db                              # Auto-generated SQLite database
├── run_all.sh                                  # Unix build & launch script
├── ARCHITECTURE.md                             # Architecture reference doc
└── README.md                                   # This file
```

---

## 🔬 Module Deep Dive

### Presentation Layer (`com.intellicase.presentation`)

| Class | Responsibility |
| :--- | :--- |
| `MainApp` | JavaFX `Application` entry point. Loads `RootLayout.fxml`, applies `CyberpunkUI.css`, and launches the primary stage in maximized/fullscreen mode. |
| `ViewRouter` | Singleton SPA router. Caches FXML-loaded `Parent` nodes and swaps them into the central `contentPane` StackPane on navigation requests. |
| `RootLayoutController` | Master controller for the persistent shell. Initializes particle engine, clock, lockdown status, guidance overlays, and global audio binding. |
| `AudioFeedbackManager` | Utility class that synthesizes raw PCM byte arrays at 44.1kHz/16-bit. Generates sine, square, and composite waveforms. Provides `bindToScene(Scene)` for recursive attachment. |
| `SecurityConsoleController` | Handles UC-07 (Shadow Profiles), UC-08 (Lockdown), UC-09 (Clearance Promotion), and UC-15 (Lockdown Deactivation) UI events. |
| `EvidenceVaultController` | Handles UC-01 (Digital Handshake), UC-03 (Audit Trail), and UC-05 (Load Score) UI events. |
| `CaseDashboardController` | Handles UC-11 (Smart Case) and UC-14 (Evidence Inventory Audit) UI events. |

### Application Layer (`com.intellicase.application`)

| Class | Responsibility |
| :--- | :--- |
| `SecurityController` | Facade for security use cases. Validates inputs, checks lockdown state, delegates to DAOs and Strategies. |
| `EvidenceController` | Facade for evidence use cases. Manages digital handshakes, load scoring, and audit trail retrieval. |
| `CaseController` | Facade for case use cases. Handles smart case creation, lockdown override, and evidence auditing. |
| `CaseFactory` | GoF Factory. Generates UUID-based `caseId` and assembles `CaseFile` entities with validated state. |
| `SystemState` | GoF Singleton. Maintains global lockdown boolean. All controllers check this before write operations. |

### Domain Layer (`com.intellicase.domain`)

| Class | Fields | Responsibility |
| :--- | :--- | :--- |
| `CaseFile` | `caseId`, `status`, `description`, `priority`, `location` | Investigation case entity with full encapsulation. |
| `Evidence` | `evidenceID`, `caseID`, `status`, `custodian`, `integrityHash`, `sensitivityLevel` | Physical evidence with chain-of-custody tracking. |
| `Agent` | `agentID`, `name`, `clearanceLevel`, `currentLoadScore` | Field agent with RBAC integer clearance. |
| `ShadowProfile` | `profileID`, `alias`, `encryptedData`, `caseID` | Encrypted confidential informant record. |
| `AuditLogEntry` | `logID`, `action`, `targetID`, `timestamp`, `actorID` | Immutable audit trail record (append-only). |

### Data Access Layer (`com.intellicase.dao`)

All DAOs use raw JDBC with `PreparedStatement` for SQL injection prevention. Each DAO maps to exactly one domain table.

---

## 🛡 Use Case Matrix & Team Division

| Developer | Area of Operation | Executed Use Cases |
| :--- | :--- | :--- |
| **Taimoor Shaukat** (`i243015`) | Security Overrides & System Control | UC-07 (Shadow Profiles), UC-08 (System Lockdown), UC-09 (Clearance Promotion) |
| **Bilal Tahir** (`i243166`) | Evidence Chain & Workload Optimization | UC-01 (Digital Handshake), UC-05 (Load Scoring), UC-03 (Audit Trail) |
| **Muhammad Ali** (`i243158`) | Auditing, Automation & Integrity | UC-11 (Smart Case), UC-15 (Lockdown Deactivation), UC-14 (Inventory Audit) |

---

## 📋 Detailed Use Case Specifications

### Security Clearance & Overrides

#### UC-07: Create Shadow Profile
- **Actor:** Security Lead / Supervisor
- **Precondition:** System lockdown is NOT active
- **Flow:** User enters Profile ID, Alias, Raw Data, and linked Case ID → `SecurityController.createShadowProfile()` validates inputs → `Aes256SimulationStrategy.encrypt()` processes raw data → `ShadowProfileDao.create()` persists encrypted record → `AuditLogDao.create()` logs the action
- **Postcondition:** Encrypted shadow profile exists in database; audit trail updated

#### UC-08: Trigger Audit Lockdown
- **Actor:** Security Lead / Director
- **Precondition:** Lockdown is currently INACTIVE
- **Flow:** User clicks Lockdown button → `SecurityController.activateAuditLockdown()` sets `SystemState.isLockdownActive = true` → `AuditLogDao.create()` logs activation → All write operations system-wide are blocked
- **Postcondition:** Global system is in read-only mode

#### UC-09: Promote Security Clearance
- **Actor:** Security Lead / Supervisor
- **Precondition:** System lockdown is NOT active; Agent exists in database
- **Flow:** User enters Agent ID → `SecurityController.promoteSecurityClearance()` validates → `AgentDao.updateClearance()` increments clearance level → `AuditLogDao.create()` logs promotion
- **Postcondition:** Agent's clearance level is incremented by 1

### Evidence Logic & Algorithms

#### UC-01: Initiate Digital Handshake
- **Actor:** Evidence Custodian
- **Precondition:** Evidence ID exists; system not locked down
- **Flow:** User enters Evidence ID and new Custodian → `EvidenceController.initiateDigitalHandshake()` validates → `EvidenceDao.updateStatus()` sets status to "In Transit" → `AuditLogDao.create()` logs the transfer
- **Postcondition:** Evidence status is "In Transit" with new custodian

#### UC-05: Smart Load Score Assignment
- **Actor:** Supervisor
- **Precondition:** Agent exists; score is valid positive integer
- **Flow:** User enters Agent ID and new Load Score → `EvidenceController.assignAgentSmartLoad()` delegates to `DefaultLoadScoreStrategy.calculate()` → `AgentDao.updateLoadScore()` persists → `AuditLogDao.create()` logs assignment
- **Postcondition:** Agent's load score updated; audit trail recorded

#### UC-03: View Immutable Audit Trail
- **Actor:** Auditor / Supervisor
- **Flow:** User enters Target ID → `EvidenceController.viewAuditTrail()` queries `AuditLogDao.findByTargetId()` → Returns chronologically ordered list of `AuditLogEntry` objects → Displayed in JavaFX `ListView`
- **Postcondition:** Audit trail displayed (read-only, no modifications possible)

### Automation & Integrity

#### UC-11: Create Smart Case
- **Actor:** Case Manager
- **Precondition:** System lockdown is NOT active
- **Flow:** User enters Status, Description, Priority, Location → `CaseController.createSmartCase()` delegates to `CaseFactory.createCase()` → UUID auto-generated → `CaseDao.create()` persists → `AuditLogDao.create()` logs creation
- **Postcondition:** New case exists with unique UUID; audit trail updated

#### UC-15: Deactivate System Audit Lockdown
- **Actor:** Director (highest clearance)
- **Precondition:** Lockdown is currently ACTIVE; valid override code provided
- **Flow:** User enters Override Code → `CaseController.deactivateAuditLockdown()` validates code against hardcoded secret → `SystemState.deactivateLockdown()` restores write access → `AuditLogDao.create()` logs deactivation
- **Postcondition:** System restored to full read/write mode

#### UC-14: Secure Evidence Inventory Audit
- **Actor:** Evidence Auditor
- **Flow:** User enters Case ID → `CaseController.secureEvidenceAudit()` queries `EvidenceDao.findByCaseId()` → For each evidence item, validates `integrityHash` against recomputed hash → Returns integrity report
- **Postcondition:** Evidence integrity status displayed; discrepancies flagged

---

## 🗃 Database Schema

```sql
-- Agents: Field personnel with RBAC clearance levels
CREATE TABLE Agents (
    agentID         TEXT PRIMARY KEY,
    name            TEXT NOT NULL,
    clearanceLevel  INTEGER DEFAULT 1,
    currentLoadScore INTEGER DEFAULT 0
);

-- Cases: Active investigations
CREATE TABLE Cases (
    caseID      TEXT PRIMARY KEY,
    status      TEXT NOT NULL,
    description TEXT,
    priority    TEXT,
    location    TEXT
);

-- Evidence: Physical evidence with chain-of-custody
CREATE TABLE Evidence (
    evidenceID       TEXT PRIMARY KEY,
    caseID           TEXT REFERENCES Cases(caseID),
    status           TEXT NOT NULL,
    custodian        TEXT,
    integrityHash    TEXT,
    sensitivityLevel INTEGER DEFAULT 1
);

-- ShadowProfiles: AES-256 encrypted informant records
CREATE TABLE ShadowProfiles (
    profileID     TEXT PRIMARY KEY,
    alias         TEXT NOT NULL,
    encryptedData TEXT,
    caseID        TEXT REFERENCES Cases(caseID)
);

-- AuditLog: Immutable, append-only action ledger
CREATE TABLE AuditLog (
    logID     INTEGER PRIMARY KEY AUTOINCREMENT,
    action    TEXT NOT NULL,
    targetID  TEXT,
    timestamp TEXT DEFAULT (datetime('now')),
    actorID   TEXT
);
```

**Design decisions:**
- SQLite chosen for zero-configuration offline deployment (no server process)
- `TEXT` primary keys enable UUID-based identification
- `AuditLog` uses `AUTOINCREMENT` to guarantee monotonically increasing log IDs
- Foreign keys maintain referential integrity between Evidence → Cases and ShadowProfiles → Cases

---

## 🔐 Security Model

| Feature | Implementation |
| :--- | :--- |
| **Encryption** | `Aes256SimulationStrategy` applies simulated AES-256 transformation on shadow profile raw data |
| **RBAC** | Integer clearance levels on `Agent` entities; promotion requires supervisor action |
| **Audit Lockdown** | `SystemState` Singleton flips global boolean; ALL controllers check before write ops |
| **SQL Injection Prevention** | All DAOs use `PreparedStatement` with parameterized queries |
| **Immutable Audit Trail** | `AuditLog` table is append-only; no UPDATE/DELETE operations exposed |
| **Evidence Integrity** | SHA-based integrity hashes stored alongside evidence records; re-verified on audit |

---

## 🧪 Testing & Quality Assurance

### JUnit 5 Test Suite

The project includes a rigorous test suite in `src/test/java/com/intellicase/`:

| Test Class | Coverage Area | Tests |
| :--- | :--- | :--- |
| `IntelliCaseBackendTest` | OOP Encapsulation | Verifies all domain fields are `private` via reflection |
| | Strategy Polymorphism | Validates `LoadScoreStrategy` and `EncryptionStrategy` swappability |
| | Null/Empty Handling | Feeds `null`, `""`, and `-500` into all controller methods |
| | Lockdown Enforcement | Confirms write operations fail when lockdown is active |
| | DAO CRUD | Tests full create/read/update cycle on `AgentDao` |
| | SQL Injection Resistance | Attempts `' OR 1=1 --` injection on `findById()` |
| | Audit Log Insertion | Verifies `AuditLogDao` write and retrieval |
| | Singleton Validation | Asserts `DatabaseConnection` and `SystemState` return same instance |
| | Factory Validation | Confirms `CaseFactory` generates non-null UUIDs |
| `DatabaseConnectionTest` | JDBC Lifecycle | Validates connection acquisition and singleton behavior |

### Checkstyle Enforcement

Automated code style checking via Maven Checkstyle Plugin (`checkstyle.xml`):
- Maximum line length: **140 characters**
- No star imports (`import java.util.*` → forbidden)
- No unused or redundant imports
- Mandatory braces on all control structures
- 4-space indentation with K&R brace style

### Running Tests

```bash
# Run all tests
mvn test

# Run with verbose output
mvn test -Dtest=IntelliCaseBackendTest -pl .

# Run checkstyle validation
mvn checkstyle:check
```

---

## ⚙️ Installation & Deployment

### Prerequisites

| Requirement | Version | Notes |
| :--- | :--- | :--- |
| **JDK** | 17 or higher | OpenJDK or Oracle JDK |
| **Maven** | 3.8+ | For dependency resolution and build |
| **Git** | 2.x+ | For cloning the repository |

> **Note:** No external database server is required. SQLite is embedded via JDBC and the `intellicase.db` file is auto-generated on first launch.

### Quick Start

```bash
# 1. Clone the secure repository
git clone https://github.com/T361/IntelliCase-FBI-Operations-Management-System.git
cd IntelliCase-FBI-Operations-Management-System

# 2. Build and launch via Maven JavaFX plugin
mvn clean javafx:run

# OR use the provided shell script
chmod +x run_all.sh
./run_all.sh
```

### Build Only (No Launch)

```bash
# Compile and run all tests
mvn clean verify

# Package as JAR
mvn clean package -DskipTests
```

---

## 🔧 Configuration & Build

### Maven POM Highlights (`pom.xml`)

| Dependency | Version | Purpose |
| :--- | :--- | :--- |
| `org.xerial:sqlite-jdbc` | 3.45.3.0 | SQLite JDBC driver |
| `org.openjfx:javafx-controls` | 21.0.3 | JavaFX UI controls |
| `org.openjfx:javafx-fxml` | 21.0.3 | FXML loader support |
| `org.openjfx:javafx-media` | 21.0.3 | Media API (legacy, not used by audio engine) |
| `org.junit.jupiter:junit-jupiter` | 5.10.2 | JUnit 5 test framework |

### Build Plugins

| Plugin | Purpose |
| :--- | :--- |
| `maven-surefire-plugin` 3.2.5 | Test execution with `useModulePath=false` for classpath mode |
| `maven-checkstyle-plugin` 3.3.1 | Code style enforcement on `validate` phase |
| `javafx-maven-plugin` 0.0.8 | `mvn javafx:run` entry point → `com.intellicase.presentation.MainApp` |

---

## 🎨 Screenshots & Visual Design

### Design System

The entire UI is themed via `CyberpunkUI.css` (~530 lines) with the following design tokens:

| Token | Value | Usage |
| :--- | :--- | :--- |
| Primary Background | `#040408` / `rgba(5,5,10,0.85)` | Root and input backgrounds |
| Neon Cyan | `#00f3ff` | Borders, titles, highlights, unfocused inputs |
| Neon Magenta | `#ff2bd6` / `#ff00e6` | Gradient accents, focused input glow |
| Success Green | `#00ff88` | Status indicators, success messages |
| Danger Red | `#ff4444` | Error messages, lockdown alerts |
| HUD Gold | `#ffe66d` | Guidance overlay borders and text |
| Text Primary | `#e0f0ff` / `#d4faff` | Body text, labels |
| Text Muted | `#444455` | Placeholder/prompt text |

### CSS Architecture

- **Base selectors** — `.root`, scrollbars, separators, tooltips
- **Component selectors** — `.nav-button`, `.action-button`, `.danger-button`, `.neon-button`
- **Card selectors** — `.nav-card`, `.form-card`, `.metric-card`, `.section-card`
- **Data display** — `.table-view`, `.list-view` with custom row highlighting
- **Input override** — `.text-field`, `.text-area` with deep dark mode and magenta focus glow
- **Overkill layer** — Global hover effects, glass panels, HUD overlays with radial gradients

---

## 🔍 Troubleshooting

| Issue | Solution |
| :--- | :--- |
| **White/light text field backgrounds** | Ensure `CyberpunkUI.css` contains the `.text-field, .text-area` override block. The CSS must include `.text-area .content` and `.text-area .viewport` transparent overrides. |
| **Audio crash (Exit 137 / PipeWire segfault)** | The `AudioFeedbackManager` uses `javax.sound.sampled` (not JavaFX media). All errors are silently caught. If audio is completely absent, check `javax.sound.sampled` availability on your JDK. |
| **JavaFX not found** | Ensure JavaFX 21 dependencies are in `pom.xml` and you're launching via `mvn javafx:run` (not `java -jar`). |
| **Database locked** | Close any SQLite browser tools that may hold a lock on `intellicase.db`. |
| **Checkstyle failures** | Run `mvn checkstyle:check` to see violations. Common: star imports, lines > 140 chars. |
| **Tests fail on CI** | Tests use in-memory SQLite (`:memory:`) and reflection to inject the connection. Ensure surefire runs with `useModulePath=false`. |

---

## 👥 Team Architecture

| Role | Developer | Student ID | Email | Use Cases |
| :--- | :--- | :--- | :--- | :--- |
| **Security Lead** | Taimoor Shaukat | i243015 | i243015@isb.nu.edu.pk | UC-07, UC-08, UC-09 |
| **Evidence Lead** | Bilal Tahir | i243166 | i243166@isb.nu.edu.pk | UC-01, UC-05, UC-03 |
| **Automation Lead** | Muhammad Ali | i243158 | i243158@isb.nu.edu.pk | UC-11, UC-15, UC-14 |

---

## 📜 Academic Compliance

This project was developed as part of the **Software Design & Architecture (SDA)** course at **FAST-NUCES Islamabad** (Spring 2026). It demonstrates mastery of:

- **Object-Oriented Programming:** Full encapsulation, inheritance readiness, polymorphic strategy interfaces
- **GRASP Principles:** Controller, Creator, Information Expert, High Cohesion, Low Coupling, Polymorphism, Indirection
- **GoF Design Patterns:** Singleton, Strategy, Factory, Observer, Proxy
- **Database Design:** Normalized SQLite schema with referential integrity and injection-safe JDBC
- **UI/UX Engineering:** Custom SPA routing, hardware-accelerated particle rendering, synthesized audio feedback
- **Testing:** JUnit 5 with reflection-based validation, edge-case fuzzing, and SQL injection resistance testing
- **Code Quality:** Automated Checkstyle enforcement with strict formatting rules

---

<p align="center">
  <b>IntelliCase v1.0.0</b> · Built with ☕ Java 17 · 🎨 JavaFX 21 · 🗄 SQLite<br/>
  <i>Securing the future of federal operations management.</i>
</p>
