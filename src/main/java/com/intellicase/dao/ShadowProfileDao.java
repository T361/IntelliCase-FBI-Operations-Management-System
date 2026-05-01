package com.intellicase.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.intellicase.application.SystemState;
import com.intellicase.data.DatabaseConnection;
import com.intellicase.domain.ShadowProfile;

/**
 * GRASP Controller for shadow profile persistence operations.
 */
public class ShadowProfileDao {
    private final DatabaseConnection databaseConnection;

    public ShadowProfileDao() {
        this.databaseConnection = DatabaseConnection.getInstance();
        ensureSchemaUpdated();
    }

    private void ensureSchemaUpdated() {
        try (java.sql.Statement stmt = databaseConnection.getConnection().createStatement()) {
            // This will throw an exception if the column already exists, which we just catch and ignore
            stmt.execute("ALTER TABLE ShadowProfiles ADD COLUMN creatorAgentId TEXT");
        } catch (SQLException ex) {
            // Column already exists or table doesn't exist yet, ignore
        }
    }

    public void create(ShadowProfile profile) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; shadow profile create blocked.");
            return;
        }
        String sql = "INSERT INTO ShadowProfiles (profileID, alias, encryptedData, caseID, creatorAgentId) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, profile.getProfileId());
            statement.setString(2, profile.getAlias());
            statement.setString(3, profile.getEncryptedData());
            statement.setString(4, profile.getCaseId());
            statement.setString(5, profile.getCreatorAgentId());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Shadow profile create failed: " + ex.getMessage());
        }
    }

    public ShadowProfile findById(String profileId) {
        String sql = "SELECT profileID, alias, encryptedData, caseID, creatorAgentId FROM ShadowProfiles WHERE profileID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, profileId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new ShadowProfile(
                        resultSet.getString("profileID"),
                        resultSet.getString("alias"),
                        resultSet.getString("encryptedData"),
                        resultSet.getString("caseID"),
                        resultSet.getString("creatorAgentId")
                    );
                }
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Shadow profile lookup failed: " + ex.getMessage());
        }
        return null;
    }

    public List<ShadowProfile> findAll() {
        List<ShadowProfile> profiles = new ArrayList<>();
        String sql = "SELECT profileID, alias, encryptedData, caseID, creatorAgentId FROM ShadowProfiles";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                profiles.add(new ShadowProfile(
                    resultSet.getString("profileID"),
                    resultSet.getString("alias"),
                    resultSet.getString("encryptedData"),
                    resultSet.getString("caseID"),
                    resultSet.getString("creatorAgentId")
                ));
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Shadow profile list failed: " + ex.getMessage());
        }
        return profiles;
    }

    public ShadowProfile findByAlias(String alias) {
        String sql = "SELECT profileID, alias, encryptedData, caseID, creatorAgentId FROM ShadowProfiles WHERE alias = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, alias);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new ShadowProfile(
                        resultSet.getString("profileID"),
                        resultSet.getString("alias"),
                        resultSet.getString("encryptedData"),
                        resultSet.getString("caseID"),
                        resultSet.getString("creatorAgentId")
                    );
                }
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Shadow profile lookup by alias failed: " + ex.getMessage());
        }
        return null;
    }
}
