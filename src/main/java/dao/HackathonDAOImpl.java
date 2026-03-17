package dao;

import database.ConnessioneDatabase;
import model.Hackathon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione PostgreSQL per l'entità Hackathon.
 */
public class HackathonDAOImpl implements HackathonDAO {

    private static final Logger LOGGER = Logger.getLogger(HackathonDAOImpl.class.getName());

    @Override
    public void createHackathon(Hackathon h) throws SQLException {
        String query = "INSERT INTO hackathon (title, location, startDate, endDate, " +
                "registrationStartDate, registrationEndDate, maxParticipants, " +
                "maxTeamSize, problemDescription) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, h.getTitle());
            ps.setString(2, h.getLocation());
            ps.setTimestamp(3, Timestamp.valueOf(h.getStartDate()));
            ps.setTimestamp(4, Timestamp.valueOf(h.getEndDate()));
            ps.setTimestamp(5, Timestamp.valueOf(h.getRegistrationStartDate()));
            ps.setTimestamp(6, Timestamp.valueOf(h.getRegistrationEndDate()));
            ps.setInt(7, h.getMaxParticipants());
            ps.setInt(8, h.getMaxTeamSize());
            ps.setString(9, h.getProblemDescription());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setHackathonId(rs.getInt(1));
                    LOGGER.log(Level.INFO, "Hackathon creato. ID assegnato: {0}", h.getHackathonId());
                }
            }
        }
    }

    @Override
    public Hackathon getHackathonById(int id) throws SQLException {
        // Specifico le colonne invece di usare SELECT * (Sonar S6905)
        String query = "SELECT hackathonId, title, location, startDate, endDate, registrationStartDate, " +
                "registrationEndDate, maxParticipants, maxTeamSize, problemDescription " +
                "FROM hackathon WHERE hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToHackathon(rs);
            }
        }
        return null;
    }

    @Override
    public List<Hackathon> getAllHackathons() throws SQLException {
        List<Hackathon> list = new ArrayList<>();
        String query = "SELECT hackathonId, title, location, startDate, endDate, registrationStartDate, " +
                "registrationEndDate, maxParticipants, maxTeamSize, problemDescription FROM hackathon";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToHackathon(rs));
            }
        }
        return list;
    }

    @Override
    public void updateProblemDescription(int hackathonId, String description) throws SQLException {
        String query = "UPDATE hackathon SET problemDescription = ? WHERE hackathonId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, description);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
            LOGGER.log(Level.INFO, "Problem description aggiornata per ID: {0}", hackathonId);
        }
    }

    @Override
    public String getOrganizerUsernameByHackathonId(int hackathonId) throws SQLException {
        String query = "SELECT u.name FROM users u JOIN organizer o ON u.userId = o.userId WHERE o.hackathonId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("name");
            }
        }
        return "Unknown Organizer";
    }

    @Override
    public int getHackathonIdWhereUserIsOrganizer(int userId) throws SQLException {
        String query = "SELECT hackathonId FROM organizer WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("hackathonId");
            }
        }
        return -1;
    }

    private Hackathon mapResultSetToHackathon(ResultSet rs) throws SQLException {
        return new Hackathon.Builder()
                .hackathonId(rs.getInt("hackathonId"))
                .title(rs.getString("title"))
                .location(rs.getString("location"))
                .startDate(rs.getTimestamp("startDate").toLocalDateTime())
                .endDate(rs.getTimestamp("endDate").toLocalDateTime())
                .registrationStartDate(rs.getTimestamp("registrationStartDate").toLocalDateTime())
                .registrationEndDate(rs.getTimestamp("registrationEndDate").toLocalDateTime())
                .maxParticipants(rs.getInt("maxParticipants"))
                .maxTeamSize(rs.getInt("maxTeamSize"))
                .problemDescription(rs.getString("problemDescription"))
                .build();
    }
}