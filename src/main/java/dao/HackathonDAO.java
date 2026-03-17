package dao;

import model.Hackathon;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione degli eventi Hackathon.
 */
public interface HackathonDAO {
    /**
     * Persiste un nuovo Hackathon.
     * @throws SQLException In caso di errore nel database.
     */
    void createHackathon(Hackathon hackathon) throws SQLException;

    /**
     * Recupera un Hackathon tramite ID.
     * @throws SQLException In caso di errore nel database.
     */
    Hackathon getHackathonById(int id) throws SQLException;

    /**
     * Recupera la lista globale degli hackathon.
     * @throws SQLException In caso di errore nel database.
     */
    List<Hackathon> getAllHackathons() throws SQLException;

    /**
     * Aggiorna la descrizione del problema.
     * @throws SQLException In caso di errore nel database.
     */
    void updateProblemDescription(int hackathonId, String description) throws SQLException;

    /**
     * Verifica se l'utente è organizzatore e restituisce l'ID dell'hackathon.
     * @throws SQLException In caso di errore nel database.
     */
    int getHackathonIdWhereUserIsOrganizer(int userId) throws SQLException;

    /**
     * Recupera l'username dell'organizzatore tramite ID evento.
     * @throws SQLException In caso di errore nel database.
     */
    String getOrganizerUsernameByHackathonId(int hackathonId) throws SQLException;
}