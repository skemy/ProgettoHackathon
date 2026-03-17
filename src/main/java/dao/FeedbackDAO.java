package dao;

import exceptions.BlankFieldException;
import model.Feedback;
import java.sql.SQLException;
import java.util.List;

public interface FeedbackDAO {
    boolean saveOrUpdateFeedback(int judgeId, int documentId, String text) throws SQLException, BlankFieldException;
    String getFeedbackText(int judgeId, int documentId) throws SQLException;
    List<Feedback> getAllFeedbacksForDocument(int documentId) throws SQLException;
}