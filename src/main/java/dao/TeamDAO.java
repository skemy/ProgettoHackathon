package dao;

import model.Team;
import model.Participant;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione della persistenza dei Team e delle partecipazioni.
 * Definisce i contratti per la creazione dei gruppi e la gestione dei membri.
 */
public interface TeamDAO {

    /**
     * Crea un nuovo team e restituisce l'ID generato.
     * @param team L'oggetto Team da persistere.
     * @return L'ID univoco assegnato dal database.
     * @throws SQLException In caso di errore di persistenza.
     */
    int createTeamAndReturnId(Team team) throws SQLException;

    /**
     * Associa un utente a un team specifico.
     * @param userId L'ID dell'utente.
     * @param teamId L'ID del team.
     * @return true se l'associazione è avvenuta con successo.
     * @throws SQLException In caso di errore di persistenza.
     */
    boolean linkUserToTeam(int userId, int teamId) throws SQLException;

    /**
     * Recupera l'ID di un team partendo dal suo codice di accesso.
     * @param code Il codice univoco del team.
     * @return L'ID del team o -1 se non trovato.
     * @throws SQLException In caso di errore di persistenza.
     */
    int getTeamIdByCode(String code) throws SQLException;

    /**
     * Recupera i dati di un team tramite il suo ID.
     * @param teamId L'ID del team.
     * @return L'oggetto Team popolato o null.
     * @throws SQLException In caso di errore di persistenza.
     */
    Team getTeamById(int teamId) throws SQLException;

    /**
     * Restituisce la lista di tutti i team iscritti a un determinato Hackathon.
     * @param hackathonId L'ID dell'evento.
     * @throws SQLException In caso di errore di persistenza.
     */
    List<Team> getTeamsByHackathon(int hackathonId) throws SQLException;

    /**
     * Recupera i membri appartenenti a un determinato team.
     * @param teamId L'ID del team.
     * @throws SQLException In caso di errore di persistenza.
     */
    List<Participant> getTeamMembers(int teamId) throws SQLException;

    /**
     * Recupera l'ID del team a cui appartiene un determinato utente.
     * @param userId L'ID dell'utente.
     * @throws SQLException In caso di errore di persistenza.
     */
    int getTeamIdByUserId(int userId) throws SQLException;

    /**
     * Recupera l'ID dell'Hackathon a cui è iscritto un determinato team.
     * @param teamId L'ID del team.
     * @throws SQLException In caso di errore di persistenza.
     */
    int getHackathonIdByTeam(int teamId) throws SQLException;
}