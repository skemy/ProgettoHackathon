package dao;

import database.ConnessioneDatabase;
import model.Hackathon;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia HackathonDAO per database PostgreSQL.
 * Gestisce la persistenza degli oggetti Hackathon utilizzando JDBC.
 */
public class HackathonDAOImpl implements HackathonDAO {

    /**
     * Inserisce un nuovo record Hackathon nel database.
     * * @param h L'oggetto Hackathon contenente i dati da salvare.
     */
    @Override
    public void createHackathon(Hackathon h) {
// Modifica la query nel metodo createHackathon di HackathonDAOImpl.java
        String query = "INSERT INTO hackathon (title, location, startDate, endDate, " +
                "registrationStartDate, registrationEndDate, maxParticipants, " +
                "maxTeamSize, problemDescription) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        // Aggiungiamo Statement.RETURN_GENERATED_KEYS qui!
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

            // 1. Eseguiamo l'inserimento
            ps.executeUpdate();

            // 2. Catturiamo l'ID appena generato e lo "iniettiamo" nell'oggetto
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerato = rs.getInt(1);
                    h.setHackathonId(idGenerato); // Aggiorniamo il Model!
                    System.out.println("✅ Hackathon '" + h.getTitle() + "' creato nel DB con ID: " + h.getHackathonId());
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la creazione dell'Hackathon: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Recupera un Hackathon tramite il suo identificativo.
     * * @param id ID dell'evento.
     * @return L'oggetto Hackathon se trovato, null altrimenti.
     */
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

    /**
     * Recupera tutti gli Hackathon registrati nel sistema.
     * * @return Una lista di oggetti Hackathon.
     */
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

    /**
     * Aggiorna la descrizione del problema/traccia per un hackathon esistente.
     * * @param hackathonId ID dell'evento da aggiornare.
     * @param description Nuovo testo della traccia.
     */
    @Override
    public void updateProblemDescription(int hackathonId, String description) {
        String query = "UPDATE hackathon SET problemDescription = ? WHERE hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, description);
            ps.setInt(2, hackathonId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Descrizione problema aggiornata per Hackathon ID: " + hackathonId);
            } else {
                System.out.println("⚠️ Nessun Hackathon trovato con ID: " + hackathonId);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Converte una riga del ResultSet in un oggetto Hackathon.
     * * @param rs ResultSet posizionato sulla riga da mappare.
     * @return Oggetto Hackathon popolato.
     * @throws SQLException In caso di errori nell'accesso ai dati del ResultSet.
     */
    private Hackathon mapResultSetToHackathon(ResultSet rs) throws SQLException {
        LocalDateTime start = rs.getTimestamp("startDate").toLocalDateTime();
        LocalDateTime end = rs.getTimestamp("endDate").toLocalDateTime();
        LocalDateTime regStart = rs.getTimestamp("registrationStartDate").toLocalDateTime();
        LocalDateTime regEnd = rs.getTimestamp("registrationEndDate").toLocalDateTime();

        return new Hackathon(
                rs.getInt("hackathonId"),
                rs.getString("title"),
                rs.getString("location"),
                start,
                end,
                regStart,
                regEnd,
                rs.getInt("maxParticipants"),
                rs.getInt("maxTeamSize"),
                rs.getString("problemDescription")
        );
    }
}