package dao;

import model.Vote;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione della persistenza dei voti.
 * Definisce i contratti per l'inserimento delle valutazioni e il calcolo delle classifiche.
 */
public interface VoteDAO {
    /**
     * Inserisce un nuovo voto espresso da un giudice per un team.
     * @param judgeId ID del giudice.
     * @param teamId ID del team.
     * @param score Punteggio assegnato.
     * @return true se l'inserimento ha avuto successo.
     * @throws SQLException In caso di errore nel database.
     */
    boolean insertVote(int judgeId, int teamId, int score) throws SQLException;

    /**
     * Verifica se un giudice ha già votato per un determinato team.
     * @throws SQLException In caso di errore nel database.
     */
    boolean checkIfAlreadyVoted(int judgeId, int teamId) throws SQLException;
    /**
     * Controlla se tutte le valutazioni attese per un hackathon sono state completate.
     * @throws SQLException In caso di errore nel database.
     */
    boolean areAllVotesCast(int hackathonId) throws SQLException;

    /**
     * Genera la classifica dei team basata sulla media dei voti ricevuti.
     * @return Una lista di stringhe formattate per la visualizzazione nella Boundary.
     * @throws SQLException In caso di errore nel database.
     */
    List<String> getLeaderboard(int hackathonId) throws SQLException;

    /**
     * Recupera l'elenco dei voti ricevuti da uno specifico team.
     * @throws SQLException In caso di errore nel database.
     */
    List<Vote> getVoteByTeam(int teamId) throws SQLException;
}