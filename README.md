# IntelliCase: FBI Operations Management System

Pure Java 17 monolith with JavaFX frontend and SQLite backend using raw JDBC.

## Requirements
- Java 17+
- SQLite JDBC driver (to be configured in build file)

## Status
- Phase 9 complete with enhanced JavaFX dashboard and backend controllers.

## Structure
- `src/main/java` — Java sources
- `src/main/resources` — FXML/CSS resources

## Notes
- Layered architecture is enforced: Presentation, Application, Domain, Data Access.

## Build & Test
- Compile: `mvn -q -DskipTests compile`
- Run tests: `mvn -q test`
- Lint (Checkstyle): `mvn -q checkstyle:check`
- Run UI: `mvn -q javafx:run`
