package dao;

import database.ConnessioneDatabase;
import model.Team;
import model.Participant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia TeamDAO per PostgreSQL.
 * Gestisce l'aggregazione degli utenti, la generazione dei codici di accesso
 * e le relazioni molti-a-molti tra Utenti e Team (tabella participation).
 */
public class TeamDAOImpl implements TeamDAO {

    private static final Logger LOGGER = Logger.getLogger(TeamDAOImpl.class.getName());

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
                    LOGGER.log(Level.INFO, "Team creato con successo. ID: {0}, Codice: {1}",
                            new Object[]{id, generatedCode});
                    return id;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la creazione del team: " + team.getTeamName(), e);
        }
        return -1;
    }

    @Override
    public boolean linkUserToTeam(int userId, int teamId) {
        String query = "INSERT INTO participation (userId, teamId) VALUES (?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            ps.setInt(2, teamId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'associazione dell'utente " + userId + " al team " + teamId, e);
        }
        return false;
    }

    @Override
    public int getTeamIdByCode(String code) {
        String query = "SELECT teamId FROM team WHERE accessCode = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, code);
            // FIX: ResultSet nel try-with-resources
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("teamId");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nella ricerca del team per codice di accesso", e);
        }
        return -1;
    }

    @Override
    public Team getTeamById(int teamId) {
        String query = "SELECT * FROM team WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            // FIX: ResultSet nel try-with-resources
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Team(
                            rs.getInt("teamId"),
                            rs.getString("teamName"),
                            rs.getString("accessCode"),
                            rs.getTimestamp("creationDate").toLocalDateTime(),
                            rs.getInt("hackathonId")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero del team con ID: " + teamId, e);
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
            // FIX: ResultSet nel try-with-resources
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Team(
                            rs.getInt("teamId"),
                            rs.getString("teamName"),
                            rs.getString("accessCode"),
                            rs.getTimestamp("creationDate").toLocalDateTime(),
                            rs.getInt("hackathonId")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero dei team per l'hackathon ID: " + hackathonId, e);
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
            // FIX: ResultSet nel try-with-resources
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(new Participant(
                            rs.getInt("userId"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            teamId
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero dei membri del team ID: " + teamId, e);
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
            LOGGER.log(Level.SEVERE, "Errore nel recupero del team per l'utente ID: " + userId, e);
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
            LOGGER.log(Level.SEVERE, "Errore nel recupero dell'hackathon per il team ID: " + teamId, e);
        }
        return -1;
    }
}