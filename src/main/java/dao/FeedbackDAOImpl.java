package dao;

import database.ConnessioneDatabase;
import model.Feedback;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAOImpl {

    /**
     * Salva o aggiorna un feedback (Logica di sovrascrittura automatica).
     */
    public boolean saveOrUpdateFeedback(int judgeId, int documentId, String text) {
        // Sintassi PostgreSQL 'ON CONFLICT' per gestire l'update se il record esiste già
        String query = "INSERT INTO feedback (text, judgeId, documentId) VALUES (?, ?, ?) " +
                "ON CONFLICT (judgeId, documentId) DO UPDATE SET text = EXCLUDED.text, feedbackDate = CURRENT_TIMESTAMP";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, text);
            ps.setInt(2, judgeId);
            ps.setInt(3, documentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera il feedback specifico di un giudice per un documento (Privacy).
     */
    public String getFeedbackText(int judgeId, int documentId) {
        String query = "SELECT text FROM feedback WHERE judgeId = ? AND documentId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, judgeId);
            ps.setInt(2, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("text");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "";
    }

    /**
     * Recupera tutti i feedback per un documento con il nome del giudice (Trasparenza Team).
     */
    public List<Feedback> getAllFeedbacksForDocument(int documentId) {
        List<Feedback> list = new ArrayList<>();
        // Query con JOIN per prendere nome e commento in un colpo solo
        String query = "SELECT f.text, f.feedbackDate, u.name as judgeName " +
                "FROM feedback f JOIN users u ON f.judgeId = u.userId " +
                "WHERE f.documentId = ? ORDER BY f.feedbackDate DESC";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Usiamo il nuovo costruttore: Commento, NomeGiudice, Data
                    list.add(new Feedback(rs.getString("text"), rs.getString("judgeName"), rs.getTimestamp("feedbackDate").toLocalDateTime()));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}