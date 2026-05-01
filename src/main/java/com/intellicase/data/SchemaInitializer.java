package com.intellicase.data;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * GRASP Controller responsible for initializing database schema.
 * Executes CREATE TABLE statements for core IntelliCase entities.
 */
public class SchemaInitializer {
    private final DatabaseConnection databaseConnection;

    public SchemaInitializer() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    public void initialize() {
        try (Statement statement = databaseConnection.getConnection().createStatement()) {
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Cases (" +
                    "caseID TEXT PRIMARY KEY, " +
                    "status TEXT, " +
                    "description TEXT, " +
                    "priority TEXT, " +
                    "location TEXT" +
                ")"
            );

            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Evidence (" +
                    "evidenceID TEXT PRIMARY KEY, " +
                    "caseID TEXT, " +
                    "status TEXT, " +
                    "custodian TEXT, " +
                    "integrityHash TEXT, " +
                    "sensitivityLevel INT" +
                ")"
            );

            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS AuditLog (" +
                    "logID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "action TEXT, " +
                    "targetID TEXT, " +
                    "actorID TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS Agents (" +
                    "agentID TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "clearanceLevel INT, " +
                    "currentLoadScore INT" +
                ")"
            );

            statement.executeUpdate("DROP TABLE IF EXISTS ShadowProfiles");
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS ShadowProfiles (" +
                    "profileID TEXT PRIMARY KEY, " +
                    "alias TEXT, " +
                    "encryptedData TEXT, " +
                    "caseID TEXT, " +
                    "creatorAgentId TEXT" +
                ")"
            );

            System.out.println("[DB] Schema initialized successfully.");
        } catch (SQLException ex) {
            System.out.println("[DB] Schema initialization failed: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SchemaInitializer initializer = new SchemaInitializer();
        initializer.initialize();
        DatabaseConnection.getInstance().close();
    }
}
