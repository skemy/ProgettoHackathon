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
    public boolean createTeam(Team team) {
        String generatedCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        team.setAccessCode(generatedCode);

        // Niente ruolo qui, e creationDate si compila da solo nel DB
        String query = "INSERT INTO team (teamName, accessCode, hackathonId) VALUES (?, ?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, team.getTeamName());
            ps.setString(2, team.getAccessCode());
            ps.setInt(3, team.getHackathonId());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        team.setTeamId(rs.getInt(1));
                    }
                }
                System.out.println("✅ Team creato con codice: " + generatedCode);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean joinTeam(Participant participant, String accessCode) {
        int teamId = getTeamIdByCode(accessCode);
        if (teamId == -1) {
            System.out.println("❌ Codice team non valido.");
            return false;
        }

        // Il Database imposterà "role" a 'MEMBER' in automatico!
        String query = "INSERT INTO participation (userId, teamId) VALUES (?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, participant.getUserId());
            ps.setInt(2, teamId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

        // Niente più estrazione del 'role'
        String query = "SELECT u.* FROM users u " +
                "JOIN participation p ON u.userId = p.userId " +
                "WHERE p.teamId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // QUI C'ERA L'ERRORE: Ora passiamo ESATTAMENTE i 5 parametri richiesti!
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

    private int getTeamIdByCode(String code) {
        String query = "SELECT teamId FROM team WHERE accessCode = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("teamId");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}