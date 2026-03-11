package dao;

import database.ConnessioneDatabase;
import model.Hackathon;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HackathonDAOImpl implements HackathonDAO {

    @Override
    public void createHackathon(Hackathon h) {
        // Query aggiornata in base al tuo nuovo DB (nessun organizerId qui)
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

            if (h.getProblemDescription() != null) {
                ps.setString(9, h.getProblemDescription());
            } else {
                ps.setNull(9, Types.VARCHAR);
            }

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setHackathonId(rs.getInt(1));
                    System.out.println("✅ Hackathon creato con ID: " + h.getHackathonId());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Hackathon getHackathonById(int id) {
        String query = "SELECT * FROM hackathon WHERE hackathonId = ?";
        Hackathon h = null;

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                h = mapResultSetToHackathon(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return h;
    }

    @Override
    public List<Hackathon> getAllHackathons() {
        List<Hackathon> list = new ArrayList<>();
        String query = "SELECT * FROM hackathon";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToHackathon(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void updateProblemDescription(int hackathonId, String description) {
        String query = "UPDATE hackathon SET problemDescription = ? WHERE hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, description);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- NUOVO METODO PER RECUPERARE IL NOME DELL'ORGANIZZATORE CON UNA JOIN ---
    public String getOrganizerUsernameByHackathonId(int hackathonId) {
        String query = "SELECT u.name FROM users u " +
                "JOIN organizer o ON u.userId = o.userId " +
                "WHERE o.hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, hackathonId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("name"); // Ritorna il nome dell'organizzatore
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown Organizer";
    }

    private Hackathon mapResultSetToHackathon(ResultSet rs) throws SQLException {
        return new Hackathon(
                rs.getInt("hackathonId"),
                rs.getString("title"),
                rs.getString("location"),
                rs.getTimestamp("startDate").toLocalDateTime(),
                rs.getTimestamp("endDate").toLocalDateTime(),
                rs.getTimestamp("registrationStartDate").toLocalDateTime(),
                rs.getTimestamp("registrationEndDate").toLocalDateTime(),
                rs.getInt("maxParticipants"),
                rs.getInt("maxTeamSize"),
                rs.getString("problemDescription")
        );
    }
}