package model;

import java.time.LocalDateTime;

public class Feedback {
    private int feedbackId;
    private String comment; // Manteniamo il tuo nome originale
    private LocalDateTime date;
    private int judgeId;
    private int documentId;

    // NUOVO CAMPO: Serve per trasportare il nome dal DAO alla GUI senza fare query extra
    private String judgeName;

    public Feedback() {}

    // Costruttore Originale
    public Feedback(int feedbackId, String comment, LocalDateTime date, int judgeId, int documentId) {
        this.feedbackId = feedbackId;
        this.comment = comment;
        this.date = date;
        this.judgeId = judgeId;
        this.documentId = documentId;
    }

    // NUOVO COSTRUTTORE: Usato dal DAO per la visualizzazione dei team
    public Feedback(String comment, String judgeName, LocalDateTime date) {
        this.comment = comment;
        this.judgeName = judgeName;
        this.date = date;
    }

    public int getFeedbackId() { return feedbackId; }
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public int getJudgeId() { return judgeId; }
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public String getJudgeName() { return judgeName; }
    public void setJudgeName(String judgeName) { this.judgeName = judgeName; }

    @Override
    public String toString() {
        return "Feedback [ID=" + feedbackId + ", Commento=" + comment + "]";
    }
}