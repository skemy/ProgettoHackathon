package dao;

import database.ConnessioneDatabase;
import model.Feedback;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione concreta dell'interfaccia FeedbackDAO per il database PostgreSQL.
 * Questa classe funge da ponte tra l'oggetto Feedback in Java e la tabella 'feedback' su SQL.
 * * Gestisce le operazioni di scrittura, lettura filtrata e validazione dell'integrità
 * per i commenti lasciati dai giudici.
 */
public class FeedbackDAOImpl implements FeedbackDAO {

    /**
     * Salva un nuovo feedback nel database.
     * Mappa l'attributo 'comment' dell'oggetto Java sulla colonna 'text' del database.
     * L'ID e la data vengono generati automaticamente dal DBMS.
     * * @param feedback L'oggetto contenente i dati del commento da persistere.
     */
    @Override
    public void saveFeedback(Feedback feedback) {
        // Nota: usiamo i nomi delle colonne definiti nel tuo script SQL (text, judgeId, documentId)
        String query = "INSERT INTO feedback (text, judgeId, documentId) VALUES (?, ?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, feedback.getComment()); // Traduzione: da Java (comment) a SQL (text)
            ps.setInt(2, feedback.getJudgeId());
            ps.setInt(3, feedback.getDocumentId());

            ps.executeUpdate();
            System.out.println("✅ Feedback salvato con successo per il Documento ID: " + feedback.getDocumentId());

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il salvataggio del feedback.");
            e.printStackTrace();
        }
    }

    /**
     * Recupera la lista di tutti i feedback associati a un determinato documento.
     * Effettua la traduzione inversa: estrae dati dalle colonne SQL (text, feedbackDate)
     * e istanzia oggetti Java di tipo Feedback.
     * * @param documentId L'ID del documento di cui si vogliono leggere i commenti.
     * @return Una lista di oggetti Feedback (vuota se non ci sono commenti).
     */
    @Override
    public List<Feedback> getFeedbackByDocument(int documentId) {
        List<Feedback> feedbackList = new ArrayList<>();
        String query = "SELECT * FROM feedback WHERE documentId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, documentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // Creazione dell'oggetto Feedback tramite costruttore completo
                    Feedback f = new Feedback(
                            rs.getInt("feedbackId"),
                            rs.getString("text"), // Nome colonna SQL
                            rs.getTimestamp("feedbackDate").toLocalDateTime(), // Conversione data
                            rs.getInt("judgeId"),
                            rs.getInt("documentId")
                    );
                    feedbackList.add(f);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante il recupero dei feedback per il documento " + documentId);
            e.printStackTrace();
        }

        return feedbackList;
    }

    /**
     * Esegue un controllo di sicurezza preventivo per verificare se un giudice
     * ha già commentato lo stesso documento, rispettando il vincolo di unicità.
     * * @param judgeId L'ID del giudice che sta tentando di commentare.
     * @param documentId L'ID del documento target.
     * @return true se esiste già un record, false altrimenti.
     */
    @Override
    public boolean hasJudgeAlreadyCommented(int judgeId, int documentId) {
        String query = "SELECT 1 FROM feedback WHERE judgeId = ? AND documentId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, judgeId);
            ps.setInt(2, documentId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Restituisce true se trova una corrispondenza
            }

        } catch (SQLException e) {
            System.err.println("❌ Errore durante la verifica della guardia feedback.");
            e.printStackTrace();
        }

        return false;
    }
}