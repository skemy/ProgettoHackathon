package dao;

import database.ConnessioneDatabase;
import model.Team;
import model.Participant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamDAOImpl implements TeamDAO {

    @Override
    public int createTeamAndReturnId(Team team) {
        // Generazione automatica dell'access code (8 caratteri alfanumerici)
        String generatedCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        team.setAccessCode(generatedCode);

        String query = "INSERT INTO team (teamName, accessCode, hackathonId) VALUES (?, ?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getAccessCode());
            ps.setInt(3, team.getHackathonId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    team.setTeamId(id);
                    System.out.println("✅ Team creato nel DB con ID: " + id + " e codice: " + generatedCode);
                    return id;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean linkUserToTeam(int userId, int teamId) {
        // Il DB gestisce il ruolo 'MEMBER' di default nella tabella participation
        String query = "INSERT INTO participation (userId, teamId) VALUES (?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setInt(2, teamId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getTeamIdByCode(String code) {
        String query = "SELECT teamId FROM team WHERE accessCode = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("teamId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public Team getTeamById(int teamId) {
        String query = "SELECT * FROM team WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Team(
                        rs.getInt("teamId"),
                        rs.getString("teamName"),
                        rs.getString("accessCode"),
                        rs.getTimestamp("creationDate").toLocalDateTime(),
                        rs.getInt("hackathonId")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Team> getTeamsByHackathon(int hackathonId) {
        List<Team> list = new ArrayList<>();
        String query = "SELECT * FROM team WHERE hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, hackathonId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Team(
                        rs.getInt("teamId"),
                        rs.getString("teamName"),
                        rs.getString("accessCode"),
                        rs.getTimestamp("creationDate").toLocalDateTime(),
                        rs.getInt("hackathonId")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Participant> getTeamMembers(int teamId) {
        List<Participant> members = new ArrayList<>();
        String query = "SELECT u.* FROM users u " +
                "JOIN participation p ON u.userId = p.userId " +
                "WHERE p.teamId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Mappatura polimorfica: passiamo (id, name, email, password, teamId)
                members.add(new Participant(
                        rs.getInt("userId"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        teamId
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return members;
    }

    @Override
    public int getTeamIdByUserId(int userId) {
        String query = "SELECT teamId FROM participation WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("teamId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public int getHackathonIdByTeam(int teamId) {
        String query = "SELECT hackathonId FROM team WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("hackathonId");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}