package dao;

import database.ConnessioneDatabase;
import model.Vote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoteDAOImpl implements VoteDAO {

    public boolean insertVote(int judgeId, int teamId, int score) {
        String query = "INSERT INTO vote (judgeId, teamId, score) VALUES (?,?,?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, judgeId);
            ps.setInt(2, teamId);
            ps.setInt(3, score);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    @Override
    public boolean checkIfAlreadyVoted(int judgeId, int teamId) {
        String query = "SELECT 1 FROM vote WHERE judgeId = ? AND teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, judgeId);
            ps.setInt(2, teamId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean areAllVotesCast(int hackathonId) {
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM jury WHERE hackathonId = ?) * " +
                "(SELECT COUNT(*) FROM team WHERE hackathonId = ?) AS expected_votes, " +
                "(SELECT COUNT(*) FROM vote v JOIN team t ON v.teamId = t.teamId WHERE t.hackathonId = ?) AS actual_votes";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, hackathonId);
            ps.setInt(2, hackathonId);
            ps.setInt(3, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int expected = rs.getInt("expected_votes");
                    int actual = rs.getInt("actual_votes");
                    return expected > 0 && expected == actual;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    /**
     * NUOVA LOGICA: Gestisce team senza documenti (0) e tie-breakers.
     * Calcola anche i pari merito.
     */
    public List<String> getLeaderboard(int hackathonId) {
        List<String> ranking = new ArrayList<>();
        // Query avanzata: LEFT JOIN per includere tutti i team.
        // COALESCE per trasformare i null in 0.
        String query = "SELECT " +
                "t.teamName, " +
                "COUNT(DISTINCT d.documentId) AS total_docs, " +
                "CASE " +
                "    WHEN COUNT(DISTINCT d.documentId) = 0 THEN 0.00 " +
                "    ELSE COALESCE(ROUND(AVG(v.score), 2), 0.00) " +
                "END AS final_score " +
                "FROM team t " +
                "LEFT JOIN document d ON t.teamId = d.teamId " +
                "LEFT JOIN vote v ON t.teamId = v.teamId " +
                "WHERE t.hackathonId = ? " +
                "GROUP BY t.teamId, t.teamName " +
                "ORDER BY final_score DESC, total_docs DESC, t.teamName ASC";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, hackathonId);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                double prevScore = -1;
                int prevDocs = -1;

                while (rs.next()) {
                    String name = rs.getString("teamName");
                    double score = rs.getDouble("final_score");
                    int docs = rs.getInt("total_docs");

                    String tieText = "";
                    // Se score e docs sono identici al precedente, è un pari merito
                    if (score == prevScore && docs == prevDocs && rank > 1) {
                        tieText = " <span style='color: orange;'>(Tied)</span>";
                        // Non incrementiamo il rank numerico per i pari merito
                    } else if (rank > 1) {
                        rank++; // Incrementa normalmente se non c'è pareggio
                    }

                    ranking.add(rank + "° Place: " + name + " - Score: " + score + " / 10" + tieText);

                    prevScore = score;
                    prevDocs = docs;
                    if(rank == 1) rank++; // Incrementiamo dopo il primo
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ranking;
    }

    @Override
    public List<Vote> getVoteByTeam(int teamId) {
        // Mantenuto per compatibilità
        return new ArrayList<>();
    }
}