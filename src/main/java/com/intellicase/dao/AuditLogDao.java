package com.intellicase.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.intellicase.application.SystemState;
import com.intellicase.data.DatabaseConnection;
import com.intellicase.domain.AuditLogEntry;

/**
 * GRASP Controller for audit log persistence operations.
 */
public class AuditLogDao {
    private final DatabaseConnection databaseConnection;

    public AuditLogDao() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    public void create(AuditLogEntry entry) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; audit log create blocked.");
            return;
        }
        String sql = "INSERT INTO AuditLog (action, targetID, actorID) VALUES (?, ?, ?)";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, entry.getAction());
            statement.setString(2, entry.getTargetId());
            statement.setString(3, entry.getActorId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Audit log create failed: " + ex.getMessage());
        }
    }

    public List<AuditLogEntry> findByTargetId(String targetId) {
        List<AuditLogEntry> logs = new ArrayList<>();
        String sql = "SELECT logID, action, targetID, actorID, timestamp FROM AuditLog WHERE targetID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, targetId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    logs.add(new AuditLogEntry(
                        resultSet.getInt("logID"),
                        resultSet.getString("action"),
                        resultSet.getString("targetID"),
                        resultSet.getString("actorID"),
                        resultSet.getString("timestamp")
                    ));
                }
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Audit log lookup failed: " + ex.getMessage());
        }
        return logs;
    }

    public List<AuditLogEntry> findAll() {
        List<AuditLogEntry> logs = new ArrayList<>();
        String sql = "SELECT logID, action, targetID, actorID, timestamp FROM AuditLog";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                logs.add(new AuditLogEntry(
                    resultSet.getInt("logID"),
                    resultSet.getString("action"),
                    resultSet.getString("targetID"),
                    resultSet.getString("actorID"),
                    resultSet.getString("timestamp")
                ));
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Audit log list failed: " + ex.getMessage());
        }
        return logs;
    }
}
