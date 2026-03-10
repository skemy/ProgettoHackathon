package dao;

import database.ConnessioneDatabase;
import model.Vote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione concreta dell'interfaccia VoteDAO per PostgreSQL.
 * Gestisce l'accesso ai dati relativi alle valutazioni numeriche dei giudici.
 */
public class VoteDAOImpl implements VoteDAO {

    @Override
    public void saveVote(Vote vote) {
        String query = "INSERT INTO vote (judgeId, teamId, score) VALUES (?,?,?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, vote.getJudgeId());
            ps.setInt(2, vote.getTeamId());
            ps.setInt(3, vote.getScore());

            ps.executeUpdate();
            System.out.println("✅ Voto salvato nel caveau per il Team ID: " + vote.getTeamId());

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il salvataggio del voto.");
            e.printStackTrace();
        }
    }

    @Override
    public List<Vote> getVoteByTeam(int teamId) {
        List<Vote> votesList = new ArrayList<>();
        // Estraiamo tutti i voti destinati a questo team
        String query = "SELECT * FROM vote WHERE teamId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                // Cicliamo su tutti i risultati estratti
                while (rs.next()) {
                    int judgeId = rs.getInt("judgeId");
                    int score = rs.getInt("score");
                    Vote v = new Vote(judgeId, teamId, score);
                    votesList.add(v);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero dei voti per il team " + teamId);
            e.printStackTrace();
        }

        return votesList;
    }

    @Override
    public boolean hasJudgeAlreadyVoted(int judgeId, int teamId) {

        String query = "SELECT 1 FROM vote WHERE judgeId = ? AND teamId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, judgeId);
            ps.setInt(2, teamId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Se il database è irraggiungibile, meglio bloccare il voto preventivamente
        return false;
    }
}