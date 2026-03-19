package dao;

import database.ConnessioneDatabase;
import model.Vote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia {@link VoteDAO} per PostgreSQL (Layer Entity Access).
 * Questa classe gestisce la persistenza delle valutazioni numeriche espresse dai giudici
 * e fornisce i metodi per il calcolo delle classifiche basate sulle medie dei voti.
 * <p>
 * Nota Architetturale: Rispetta pienamente gli standard SonarQube eliminando le stringhe
 * letterali duplicate tramite costanti e ottimizzando le query SQL. Le eccezioni
 * sono propagate al Controller per una gestione centralizzata della business logic.
 */
public class VoteDAOImpl implements VoteDAO {

    private static final Logger LOGGER = Logger.getLogger(VoteDAOImpl.class.getName());

    // Costanti per la risoluzione dell'issue SonarQube S1192 (Duplicate Literals)
    private static final String TEAM_ID_COL = "teamId";
    private static final String SCORE_COL = "score";
    private static final String JUDGE_ID_COL = "judgeId";

    /**
     * Inserisce un nuovo voto espresso da un giudice per un team specifico.
     *
     * @param judgeId L'ID dell'utente con ruolo di giudice.
     * @param teamId  L'ID del team da valutare.
     * @param score   Il punteggio assegnato (range 0-10).
     * @return true se l'inserimento è avvenuto con successo.
     * @throws SQLException In caso di errore durante l'operazione di INSERT.
     */
    @Override
    public boolean insertVote(int judgeId, int teamId, float score) throws SQLException {
        String query = "INSERT INTO vote (judgeId, teamId, score) VALUES (?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, judgeId);
            ps.setInt(2, teamId);
            ps.setFloat(3, score);

            boolean success = ps.executeUpdate() > 0;
            if (success) {
                // Utilizzo dei placeholder per performance di logging (Sonar S3457)
                LOGGER.log(Level.INFO, "Voto inserito: Judge {0} -> Team {1} (Score: {2})",
                        new Object[]{judgeId, teamId, score});
            }
            return success;
        }
    }

    /**
     * Verifica l'esistenza di una valutazione già espressa da un giudice per un team.
     * Utilizzato per implementare il vincolo di unicità della valutazione.
     *
     * @param judgeId L'ID del giudice.
     * @param teamId  L'ID del team.
     * @return true se il voto è già presente nel database.
     * @throws SQLException In caso di errore durante l'interrogazione.
     */
    @Override
    public boolean checkIfAlreadyVoted(int judgeId, int teamId) throws SQLException {
        String query = "SELECT 1 FROM vote WHERE judgeId = ? AND teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, judgeId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Controlla se tutti i giudici assegnati hanno completato le votazioni per tutti i team.
     *
     * @param hackathonId L'identificativo dell'hackathon di riferimento.
     * @return true se il numero di voti attesi coincide con quelli presenti.
     * @throws SQLException In caso di errore nel calcolo aggregato.
     */
    @Override
    public boolean areAllVotesCast(int hackathonId) throws SQLException {
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM jury WHERE hackathonId = ?) * " +
                "(SELECT COUNT(*) FROM team WHERE hackathonId = ?) AS expected, " +
                "(SELECT COUNT(*) FROM vote v JOIN team t ON v.teamId = t.teamId WHERE t.hackathonId = ?) AS actual";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            ps.setInt(2, hackathonId);
            ps.setInt(3, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int expected = rs.getInt("expected");
                    int actual = rs.getInt("actual");
                    return expected > 0 && expected == actual;
                }
            }
        }
        return false;
    }

    /**
     * Calcola la classifica dei team in base alla media dei voti ricevuti.
     * Utilizza funzioni di aggregazione SQL per garantire efficienza.
     *
     * @param hackathonId L'ID dell'hackathon.
     * @return Una lista di stringhe formattate rappresentanti la graduatoria.
     * @throws SQLException In caso di errore SQL o nel calcolo della media.
     */
    @Override
    public List<String> getLeaderboard(int hackathonId) throws SQLException {
        List<String> ranking = new ArrayList<>();

        // CORREZIONE: Aggiunto ::numeric per permettere a ROUND di funzionare con AVG di FLOAT
        String query = "SELECT t.teamName, " +
                "COALESCE(ROUND(AVG(v.score)::numeric, 2), 0.00) AS final_score " +
                "FROM team t LEFT JOIN vote v ON t.teamId = v.teamId " +
                "WHERE t.hackathonId = ? " +
                "GROUP BY t.teamId, t.teamName " +
                "ORDER BY final_score DESC, t.teamName ASC";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    // Utilizziamo getDouble o getFloat per leggere il valore decimale
                    ranking.add(rank++ + " Position: " + rs.getString("teamName") +
                            " - Average: " + rs.getDouble("final_score") + " / 10");
                }
            }
        }
        return ranking;
    }

    /**
     * Recupera l'elenco di tutte le valutazioni singole assegnate a un team specifico.
     *
     * @param teamId L'ID del team.
     * @return Una lista di oggetti {@link Vote}.
     * @throws SQLException In caso di errore SQL o mapping.
     */
    @Override
    public List<Vote> getVoteByTeam(int teamId) throws SQLException {
        List<Vote> votes = new ArrayList<>();
        // FIX SonarQube: Elenco esplicito delle colonne invece di SELECT * (Sonar S6905)
        String query = "SELECT voteId, judgeId, teamId, score FROM vote WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    votes.add(new Vote(
                            rs.getInt("voteId"),
                            rs.getInt(JUDGE_ID_COL),
                            rs.getInt(TEAM_ID_COL),
                            rs.getInt(SCORE_COL)
                    ));
                }
            }
        }
        return votes;
    }
}