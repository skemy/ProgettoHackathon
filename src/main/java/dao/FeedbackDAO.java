package dao;

import model.Feedback;
import java.util.List;

/**
 * Interfaccia DAO per la gestione dei Feedback.
 */
public interface FeedbackDAO {
    boolean saveOrUpdateFeedback(int judgeId, int documentId, String text);
    String getFeedbackText(int judgeId, int documentId);
    List<Feedback> getAllFeedbacksForDocument(int documentId);
}