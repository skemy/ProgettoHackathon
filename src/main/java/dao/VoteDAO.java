package dao;

import model.Vote;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei voti (Score) assegnati dai giudici ai team.
 * Definisce i contratti per il salvataggio, il recupero e le validazioni di sicurezza.
 */
public interface VoteDAO {

    /**
     * Salva in modo permanente il voto assegnato da un giudice a un team.
     * * @param vote L'oggetto Vote contenente judgeId, teamId e score.
     */
    void saveVote(Vote vote);

    /**
     * Recupera l'elenco di tutti i voti ricevuti da un determinato team.
     * Questo metodo è fondamentale per il Controller per poter calcolare la media dei punteggi.
     * * @param teamId L'identificativo univoco del team.
     * @return Una lista di oggetti Vote associati al team.
     */
    List<Vote> getVoteByTeam(int teamId);

    /**
     * Verifica se un giudice ha già espresso una valutazione per uno specifico team.
     * Agisce da "Guardia" preventiva per la GUI, evitando violazioni di vincoli sul DB.
     * * @param judgeId L'ID del giudice.
     * @param teamId L'ID del team.
     * @return true se il giudice ha già votato questo team, false altrimenti.
     */
    boolean hasJudgeAlreadyVoted(int judgeId, int teamId);
}