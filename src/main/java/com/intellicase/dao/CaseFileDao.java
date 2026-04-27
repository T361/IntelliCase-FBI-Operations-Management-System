package com.intellicase.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.intellicase.application.SystemState;
import com.intellicase.data.DatabaseConnection;
import com.intellicase.domain.CaseFile;

/**
 * GRASP Controller for case persistence operations.
 * Uses raw JDBC with prepared statements.
 */
public class CaseFileDao {
    private final DatabaseConnection databaseConnection;

    public CaseFileDao() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    public void create(CaseFile caseFile) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; case create blocked.");
            return;
        }
        String sql = "INSERT INTO Cases (caseID, status, description, priority, location) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, caseFile.getCaseId());
            statement.setString(2, caseFile.getStatus());
            statement.setString(3, caseFile.getDescription());
            statement.setString(4, caseFile.getPriority());
            statement.setString(5, caseFile.getLocation());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Case create failed: " + ex.getMessage());
        }
    }

    public void updateStatus(String caseId, String status) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; case update blocked.");
            return;
        }
        String sql = "UPDATE Cases SET status = ? WHERE caseID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, caseId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Case update failed: " + ex.getMessage());
        }
    }

    public CaseFile findById(String caseId) {
        String sql = "SELECT caseID, status, description, priority, location FROM Cases WHERE caseID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, caseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new CaseFile(
                        resultSet.getString("caseID"),
                        resultSet.getString("status"),
                        resultSet.getString("description"),
                        resultSet.getString("priority"),
                        resultSet.getString("location")
                    );
                }
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Case lookup failed: " + ex.getMessage());
        }
        return null;
    }

    public List<CaseFile> findAll() {
        List<CaseFile> cases = new ArrayList<>();
        String sql = "SELECT caseID, status, description, priority, location FROM Cases";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                cases.add(new CaseFile(
                    resultSet.getString("caseID"),
                    resultSet.getString("status"),
                    resultSet.getString("description"),
                    resultSet.getString("priority"),
                    resultSet.getString("location")
                ));
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Case list failed: " + ex.getMessage());
        }
        return cases;
    }
}
