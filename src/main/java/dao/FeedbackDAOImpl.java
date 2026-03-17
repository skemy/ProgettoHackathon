package dao;

import database.ConnessioneDatabase;
import exceptions.BlankFieldException;
import model.Feedback;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAOImpl implements FeedbackDAO {
    @Override
    public boolean saveOrUpdateFeedback(int judgeId, int documentId, String text) throws SQLException, BlankFieldException {
        // La validazione qui rende il "throws BlankFieldException" nel Controller non ridondante
        if (text == null || text.trim().isEmpty()) {
            throw new BlankFieldException("Feedback text cannot be empty.");
        }

        String query = "INSERT INTO feedback (text, judgeId, documentId) VALUES (?, ?, ?) " +
                "ON CONFLICT (judgeId, documentId) DO UPDATE SET text = EXCLUDED.text";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, text);
            ps.setInt(2, judgeId);
            ps.setInt(3, documentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public String getFeedbackText(int judgeId, int documentId) throws SQLException {
        String query = "SELECT text FROM feedback WHERE judgeId = ? AND documentId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, judgeId);
            ps.setInt(2, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("text");
            }
        }
        return "";
    }

    @Override
    public List<Feedback> getAllFeedbacksForDocument(int documentId) throws SQLException {
        List<Feedback> list = new ArrayList<>();
        // 1. Aggiunta della colonna feedbackDate nella query
        String query = "SELECT f.text, f.feedbackDate, u.name as judgeName " +
                "FROM feedback f JOIN users u ON f.judgeId = u.userId " +
                "WHERE f.documentId = ? ORDER BY f.feedbackDate DESC";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // 2. Recupero del Timestamp e conversione in LocalDateTime
                    java.sql.Timestamp ts = rs.getTimestamp("feedbackDate");
                    java.time.LocalDateTime date = (ts != null) ? ts.toLocalDateTime() : java.time.LocalDateTime.now();

                    // 3. Passaggio della data reale al costruttore (niente più null!)
                    list.add(new Feedback(rs.getString("text"), rs.getString("judgeName"), date));
                }
            }
        }
        return list;
    }
}