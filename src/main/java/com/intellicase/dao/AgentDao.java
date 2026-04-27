package com.intellicase.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.intellicase.application.SystemState;
import com.intellicase.data.DatabaseConnection;
import com.intellicase.domain.Agent;

/**
 * GRASP Controller for agent persistence operations.
 */
public class AgentDao {
    private final DatabaseConnection databaseConnection;

    public AgentDao() {
        this.databaseConnection = DatabaseConnection.getInstance();
    }

    public void create(Agent agent) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; agent create blocked.");
            return;
        }
        String sql = "INSERT INTO Agents (agentID, name, clearanceLevel, currentLoadScore) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, agent.getAgentId());
            statement.setString(2, agent.getName());
            statement.setInt(3, agent.getClearanceLevel());
            statement.setInt(4, agent.getCurrentLoadScore());
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Agent create failed: " + ex.getMessage());
        }
    }

    public void updateClearance(String agentId, int clearanceLevel) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; clearance update blocked.");
            return;
        }
        String sql = "UPDATE Agents SET clearanceLevel = ? WHERE agentID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, clearanceLevel);
            statement.setString(2, agentId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Agent clearance update failed: " + ex.getMessage());
        }
    }

    public void updateLoadScore(String agentId, int loadScore) {
        if (SystemState.getInstance().isLockdownActive()) {
            System.out.println("[DAO] Lockdown active; load update blocked.");
            return;
        }
        String sql = "UPDATE Agents SET currentLoadScore = ? WHERE agentID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setInt(1, loadScore);
            statement.setString(2, agentId);
            statement.executeUpdate();
        } catch (SQLException ex) {
            System.out.println("[DAO] Agent load update failed: " + ex.getMessage());
        }
    }

    public Agent findById(String agentId) {
        String sql = "SELECT agentID, name, clearanceLevel, currentLoadScore FROM Agents WHERE agentID = ?";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, agentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Agent(
                        resultSet.getString("agentID"),
                        resultSet.getString("name"),
                        resultSet.getInt("clearanceLevel"),
                        resultSet.getInt("currentLoadScore")
                    );
                }
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Agent lookup failed: " + ex.getMessage());
        }
        return null;
    }

    public List<Agent> findAll() {
        List<Agent> agents = new ArrayList<>();
        String sql = "SELECT agentID, name, clearanceLevel, currentLoadScore FROM Agents";
        try (PreparedStatement statement = databaseConnection.getConnection().prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                agents.add(new Agent(
                    resultSet.getString("agentID"),
                    resultSet.getString("name"),
                    resultSet.getInt("clearanceLevel"),
                    resultSet.getInt("currentLoadScore")
                ));
            }
        } catch (SQLException ex) {
            System.out.println("[DAO] Agent list failed: " + ex.getMessage());
        }
        return agents;
    }
}
