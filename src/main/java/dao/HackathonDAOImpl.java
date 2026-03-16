package dao;

import database.ConnessioneDatabase;
import model.Hackathon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia HackathonDAO per PostgreSQL.
 * Gestisce il ciclo di vita dell'entità Hackathon (creazione, lettura, aggiornamento).
 * <p>
 * Nota Architetturale: Questa classe utilizza il Builder Pattern per ricostruire
 * l'entità complessa dal ResultSet, garantendo immutabilità parziale e codice pulito.
 */
public class HackathonDAOImpl implements HackathonDAO {

    private static final Logger LOGGER = Logger.getLogger(HackathonDAOImpl.class.getName());

    /**
     * Inserisce un nuovo evento Hackathon nel database.
     * Recupera automaticamente l'ID generato dal DB e lo assegna all'oggetto passato.
     *
     * @param h L'oggetto Hackathon da persistere.
     */
    @Override
    public void createHackathon(Hackathon h) {
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

            // Recupero della chiave primaria (SERIAL) generata da PostgreSQL
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    h.setHackathonId(rs.getInt(1));
                    LOGGER.log(Level.INFO, "Hackathon creato con successo. ID: {0}", h.getHackathonId());
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore SQL durante la creazione dell'hackathon", e);
        }
    }

    /**
     * Recupera un hackathon specifico dal database.
     *
     * @param id L'identificativo univoco dell'hackathon.
     * @return L'entità Hackathon trovata, o null se non esiste.
     */
    @Override
    public Hackathon getHackathonById(int id) {
        String query = "SELECT * FROM hackathon WHERE hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToHackathon(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante il recupero dell'hackathon con ID: " + id, e);
        }
        return null;
    }

    /**
     * Recupera l'elenco di tutti gli hackathon presenti nel sistema.
     *
     * @return Una lista contenente tutti gli Hackathon.
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
            LOGGER.log(Level.SEVERE, "Errore durante il recupero globale degli hackathon", e);
        }
        return list;
    }

    /**
     * Aggiorna la descrizione del problema di un hackathon esistente.
     *
     * @param hackathonId L'ID dell'evento da modificare.
     * @param description Il nuovo testo della problem description.
     */
    @Override
    public void updateProblemDescription(int hackathonId, String description) {
        String query = "UPDATE hackathon SET problemDescription = ? WHERE hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, description);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
            LOGGER.log(Level.INFO, "Problem description aggiornata per Hackathon ID: {0}", hackathonId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiornamento della problem description", e);
        }
    }

    /**
     * Esegue una JOIN per recuperare il nome dell'utente che ha organizzato l'evento.
     *
     * @param hackathonId L'ID dell'evento.
     * @return Il nome dell'organizzatore o "Unknown Organizer" in caso di errore/assenza.
     */
    @Override
    public String getOrganizerUsernameByHackathonId(int hackathonId) {
        String query = "SELECT u.name FROM users u " +
                "JOIN organizer o ON u.userId = o.userId " +
                "WHERE o.hackathonId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero del nome dell'organizzatore", e);
        }
        return "Unknown Organizer";
    }

    /**
     * Verifica se un determinato utente è organizzatore di un hackathon e ne restituisce l'ID.
     *
     * @param userId L'ID dell'utente da controllare.
     * @return L'ID dell'hackathon se l'utente è organizzatore, -1 altrimenti.
     */
    @Override
    public int getHackathonIdWhereUserIsOrganizer(int userId) {
        String query = "SELECT hackathonId FROM organizer WHERE userId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("hackathonId");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante la verifica dell'organizzatore per user ID: " + userId, e);
        }
        return -1;
    }

    /**
     * Mappa un ResultSet del database in un'entità Hackathon utilizzando il Builder Pattern.
     */
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