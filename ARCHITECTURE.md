# IntelliCase Architecture Map

## Layered Architecture
- **Presentation**: (Phase 6) JavaFX views/controllers only.
- **Application/Business Logic**: Controllers for UC flows.
- **Domain**: Pure POJOs (CaseFile, Evidence, Agent, ShadowProfile, AuditLogEntry).
- **Data Access**: JDBC DAOs and schema initializer.

## Class-to-Pattern Mapping (Initial)
| Class | Layer | GRASP/GoF Role | Notes |
| --- | --- | --- | --- |
| `DatabaseConnection` | Data Access | GoF Singleton | Raw JDBC connection provider. |
| `SchemaInitializer` | Data Access | GRASP Controller | Builds DB schema. |
| `CaseFile` | Domain | GRASP Information Expert | Case data encapsulation. |
| `Evidence` | Domain | GRASP Information Expert | Evidence data encapsulation. |
| `AuditLogEntry` | Domain | GRASP Information Expert | Audit entry data encapsulation. |
| `Agent` | Domain | GRASP Information Expert | Agent data encapsulation. |
| `ShadowProfile` | Domain | GRASP Information Expert | Shadow profile data encapsulation. |
| `CaseFileDao` | Data Access | GRASP Controller | JDBC access for cases. |
| `EvidenceDao` | Data Access | GRASP Controller | JDBC access for evidence. |
| `AuditLogDao` | Data Access | GRASP Controller | JDBC access for audit logs. |
| `AgentDao` | Data Access | GRASP Controller | JDBC access for agents. |
| `ShadowProfileDao` | Data Access | GRASP Controller | JDBC access for shadow profiles. |
| `SystemState` | Application | GoF Singleton | Global audit lockdown state. |
| `EncryptionStrategy` | Application | GoF Strategy | Shadow profile encryption abstraction. |
| `Aes256SimulationStrategy` | Application | GoF Strategy | Base64 simulation of encryption. |
| `SecurityController` | Application | GRASP Controller | UC-07/08/09 business logic. |
| `LoadScoreStrategy` | Application | GoF Strategy | Agent workload scoring. |
| `DefaultLoadScoreStrategy` | Application | GoF Strategy | Threshold-based load scoring. |
| `EvidenceController` | Application | GRASP Controller | UC-01/03/05 business logic. |
| `CaseFactory` | Application | GoF Factory | Creates CaseFile instances. |
| `CaseNotificationObserver` | Application | GoF Observer | Notified on case creation. |
| `ConsoleNotificationObserver` | Application | GoF Observer | Console-based notification sink. |
| `CaseNotificationPublisher` | Application | GoF Observer | Manages observers and broadcasts. |
| `EvidenceAuditService` | Application | GoF Proxy | Evidence audit access contract. |
| `EvidenceAuditServiceImpl` | Application | GoF Proxy | Core evidence audit service. |
| `EvidenceAuditProxy` | Application | GoF Proxy | Enforces clearance + integrity checks. |
| `CaseController` | Application | GRASP Controller | UC-11/14/15 business logic. |
| `MainDashboard.fxml` | Presentation | UI Layout | JavaFX dashboard layout. |
| `CyberpunkUI.css` | Presentation | UI Theme | Glassmorphic neon styling. |
| `DashboardController` | Presentation | GRASP Controller | UI orchestration only. |
| `MainApp` | Presentation | Application Shell | JavaFX entry point. |

## Domain Model Entities (Planned)
- CaseFile
- Evidence
- Agent
- ShadowProfile
- AuditLogEntry

## Observers/Strategies/Factories (Planned)
- Observer: Case creation notifications.
- Strategy: Agent load scoring; ShadowProfile encryption.
- Factory: Smart Case Initializer.
- Proxy: Secure evidence audit.
