package com.intellicase.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * GoF Singleton providing a shared SQLite connection.
 * GRASP: Low Coupling by centralizing DB access in one class.
 */
public final class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            this.connection = DriverManager.getConnection("jdbc:sqlite:intellicase.db");
            System.out.println("[DB] Connected to SQLite database.");
        } catch (SQLException ex) {
            System.out.println("[DB] Connection failed: " + ex.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException ex) {
                System.out.println("[DB] Close failed: " + ex.getMessage());
            }
        }
    }
}
