package com.intellicase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.intellicase.data.DatabaseConnection;
import com.intellicase.data.SchemaInitializer;

/**
 * Basic smoke tests for database connectivity and schema creation.
 */
class DatabaseConnectionTest {
    @Test
    void connectionIsAvailable() {
        DatabaseConnection connection = DatabaseConnection.getInstance();
        assertNotNull(connection.getConnection(), "Connection should be initialized");
    }

    @Test
    void schemaInitializerRuns() {
        SchemaInitializer initializer = new SchemaInitializer();
        initializer.initialize();
    }
}
