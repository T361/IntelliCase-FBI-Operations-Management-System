package com.intellicase.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.intellicase.application.SystemState;
import com.intellicase.data.DatabaseConnection;
import com.intellicase.domain.Evidence;

/**
 * GRASP Controller for evidence persistence operations.
 */
public class EvidenceDao {
    private final DatabaseConnection databaseConnection;

    public EvidenceDao() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    public void create(Evidence evidence) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; evidence create blocked.");
            return;
        }
        String sql = "INSERT INTO Evidence (evidenceID, caseID, status, custodian, integrityHash, sensitivityLevel) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, evidence.getEvidenceId());
            statement.setString(2, evidence.getCaseId());
            statement.setString(3, evidence.getStatus());
            statement.setString(4, evidence.getCustodian());
            statement.setString(5, evidence.getIntegrityHash());
            statement.setInt(6, evidence.getSensitivityLevel());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Evidence create failed: " + ex.getMessage());
        }
    }

    public void updateStatus(String evidenceId, String status) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; evidence update blocked.");
            return;
        }
        String sql = "UPDATE Evidence SET status = ? WHERE evidenceID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, evidenceId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Evidence update failed: " + ex.getMessage());
        }
    }

    public Evidence findById(String evidenceId) {
        String sql = "SELECT evidenceID, caseID, status, custodian, integrityHash, sensitivityLevel " +
            "FROM Evidence WHERE evidenceID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, evidenceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Evidence(
                        resultSet.getString("evidenceID"),
                        resultSet.getString("caseID"),
                        resultSet.getString("status"),
                        resultSet.getString("custodian"),
                        resultSet.getString("integrityHash"),
                        resultSet.getInt("sensitivityLevel")
                    );
                }
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Evidence lookup failed: " + ex.getMessage());
        }
        return null;
    }

    public List<Evidence> findAll() {
        List<Evidence> evidence = new ArrayList<>();
        String sql = "SELECT evidenceID, caseID, status, custodian, integrityHash, sensitivityLevel FROM Evidence";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                evidence.add(new Evidence(
                    resultSet.getString("evidenceID"),
                    resultSet.getString("caseID"),
                    resultSet.getString("status"),
                    resultSet.getString("custodian"),
                    resultSet.getString("integrityHash"),
                    resultSet.getInt("sensitivityLevel")
                ));
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Evidence list failed: " + ex.getMessage());
        }
        return evidence;
    }
}
