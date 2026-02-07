package model;

import java.time.LocalDateTime;

/**
 * Rappresenta il feedback fornito da un giudice in merito a un documento specifico.
 * Contiene il commento testuale, la data di inserimento e i riferimenti al giudice e al documento.
 */
public class Feedback {

    private int feedbackId;
    private String comment;
    private LocalDateTime date;
    private int judgeId;
    private int documentId;

    /**
     * Costruttore vuoto della classe Feedback.
     */
    public Feedback() {}

    /**
     * Costruttore completo della classe Feedback.
     * * @param feedbackId L'ID univoco del feedback.
     * @param comment Il testo del feedback fornito dal giudice.
     * @param date La data e l'ora in cui il feedback è stato registrato.
     * @param judgeId L'ID dell'utente (Giudice) che ha rilasciato il feedback.
     * @param documentId L'ID del documento a cui il feedback si riferisce.
     */
    public Feedback(int feedbackId, String comment, LocalDateTime date, int judgeId, int documentId) {
        this.feedbackId = feedbackId;
        this.comment = comment;
        this.date = date;
        this.judgeId = judgeId;
        this.documentId = documentId;
    }

    /**
     * @return L'ID univoco del feedback.
     */
    public int getFeedbackId() {
        return feedbackId;
    }

    /**
     * @param feedbackId L'ID univoco da assegnare al feedback.
     */
    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    /**
     * @return Il commento del giudice.
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment Il testo del commento da impostare.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return La data e l'ora del feedback.
     */
    public LocalDateTime getDate() {
        return date;
    }

    /**
     * @param date La data e l'ora da associare al feedback.
     */
    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    /**
     * @return L'ID dell'utente giudice.
     */
    public int getJudgeId() {
        return judgeId;
    }

    /**
     * @param judgeId L'ID dell'utente giudice da associare.
     */
    public void setJudgeId(int judgeId) {
        this.judgeId = judgeId;
    }

    /**
     * @return L'ID del documento valutato.
     */
    public int getDocumentId() {
        return documentId;
    }

    /**
     * @param documentId L'ID del documento da associare.
     */
    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    /**
     * Restituisce una rappresentazione testuale dell'oggetto Feedback.
     * * @return Stringa contenente ID e commento.
     */
    @Override
    public String toString() {
        return "Feedback [ID=" + feedbackId + ", Commento=" + comment + "]";
    }
}