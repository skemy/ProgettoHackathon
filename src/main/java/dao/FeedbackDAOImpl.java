package dao;

import database.ConnessioneDatabase;
import exceptions.BlankFieldException;
import model.Feedback;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia FeedbackDAO per la gestione dei feedback nel database.
 * Questa classe fornisce metodi per salvare, aggiornare, recuperare e ottenere feedback associati a giudici e documenti.
 */
public class FeedbackDAOImpl implements FeedbackDAO {
    /**
     * Salva o aggiorna un feedback per un giudice e un documento specifico.
     * @param judgeId L'ID del giudice.
     * @param documentId L'ID del documento.
     * @param text Il testo del feedback.
     * @return true se l'operazione è riuscita, false altrimenti.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     * @throws BlankFieldException Se il testo del feedback è vuoto o nullo.
     */
    @Override
    public boolean saveOrUpdateFeedback(int judgeId, int documentId, String text) throws SQLException, BlankFieldException {
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

    /**
     * Recupera il testo del feedback per un giudice e un documento specifico.
     * @param judgeId L'ID del giudice.
     * @param documentId L'ID del documento.
     * @return Il testo del feedback, o una stringa vuota se non trovato.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
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

    /**
     * Recupera tutti i feedback per un documento specifico.
     * @param documentId L'ID del documento.
     * @return Una lista di oggetti Feedback.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public List<Feedback> getAllFeedbacksForDocument(int documentId) throws SQLException {
        List<Feedback> list = new ArrayList<>();
        String query = "SELECT f.text, f.feedbackDate, u.name as judgeName " +
                "FROM feedback f JOIN users u ON f.judgeId = u.userId " +
                "WHERE f.documentId = ? ORDER BY f.feedbackDate DESC";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Timestamp ts = rs.getTimestamp("feedbackDate");
                    java.time.LocalDateTime date = (ts != null) ? ts.toLocalDateTime() : java.time.LocalDateTime.now();

                    list.add(new Feedback(rs.getString("text"), rs.getString("judgeName"), date));
                }
            }
        }
        return list;
    }
}