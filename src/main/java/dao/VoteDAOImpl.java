package dao;

import database.ConnessioneDatabase;
import model.Vote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione della VoteDAO per PostgreSQL.
 * Gestisce l'inserimento dei voti e il calcolo della classifica (Leaderboard).
 */
public class VoteDAOImpl implements VoteDAO {

    private static final Logger LOGGER = Logger.getLogger(VoteDAOImpl.class.getName());

    @Override
    public boolean insertVote(int judgeId, int teamId, int score) {
        String query = "INSERT INTO vote (judgeId, teamId, score) VALUES (?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, judgeId);
            ps.setInt(2, teamId);
            ps.setInt(3, score);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'inserimento del voto", e);
            return false;
        }
    }

    @Override
    public boolean checkIfAlreadyVoted(int judgeId, int teamId) {
        String query = "SELECT 1 FROM vote WHERE judgeId = ? AND teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, judgeId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore verifica voto esistente", e);
            return false;
        }
    }

    @Override
    public boolean areAllVotesCast(int hackathonId) {
        // Confronta il numero di voti attesi (Giudici * Team) con quelli presenti
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
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore calcolo integrità voti", e);
        }
        return false;
    }

    @Override
    public List<String> getLeaderboard(int hackathonId) {
        List<String> ranking = new ArrayList<>();
        String query = "SELECT t.teamName, " +
                "COALESCE(ROUND(AVG(v.score), 2), 0.00) AS final_score " +
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
                    ranking.add(rank++ + "° Posizione: " + rs.getString("teamName") +
                            " - Media: " + rs.getDouble("final_score") + " / 10");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore generazione classifica", e);
        }
        return ranking;
    }

    @Override
    public List<Vote> getVoteByTeam(int teamId) {
        List<Vote> votes = new ArrayList<>();
        String query = "SELECT * FROM vote WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    votes.add(new Vote(
                            rs.getInt("voteId"),
                            rs.getInt("judgeId"),
                            rs.getInt("teamId"),
                            rs.getInt("score")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore recupero voti per team", e);
        }
        return votes;
    }
}