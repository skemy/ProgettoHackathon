package model;

import java.time.LocalDateTime;

/**
 * Rappresenta l'entità di dominio Feedback.
 * Contiene le valutazioni fornite dai giudici sui documenti sottomessi dai team.
 * <p>
 * Nota Architetturale: Il campo {@code judgeName} agisce da Data Transfer Object (DTO) intrinseco.
 * Viene popolato dal layer DAO tramite un'operazione di JOIN SQL per trasportare il nome
 * fino alla View (Boundary) in una singola operazione, riducendo il carico sul database
 * ed evitando query ausiliarie (N+1 query problem).
 */
public class Feedback {

    private int feedbackId;
    private String comment;
    private LocalDateTime date;
    private int judgeId;
    private int documentId;
    private String judgeName;

    /**
     * Costruttore vuoto di default.
     */
    public Feedback() {
    }

    /**
     * Costruttore principale per la persistenza e il recupero dell'entità nativa.
     *
     * @param feedbackId L'ID univoco del feedback.
     * @param comment    Il testo della valutazione.
     * @param date       La data in cui il feedback è stato rilasciato.
     * @param judgeId    L'ID del giudice autore della valutazione.
     * @param documentId L'ID del documento valutato.
     */
    public Feedback(int feedbackId, String comment, LocalDateTime date, int judgeId, int documentId) {
        this.feedbackId = feedbackId;
        this.comment = comment;
        this.date = date;
        this.judgeId = judgeId;
        this.documentId = documentId;
    }

    /**
     * Costruttore orientato alla visualizzazione (View-Model).
     * Utilizzato dal layer DAO per istanziare oggetti arricchiti con il nome del giudice.
     *
     * @param comment   Il testo della valutazione.
     * @param judgeName Il nome completo del giudice, recuperato tramite JOIN.
     * @param date      La data di rilascio del feedback.
     */
    public Feedback(String comment, String judgeName, LocalDateTime date) {
        this.comment = comment;
        this.judgeName = judgeName;
        this.date = date;
    }

    // --- GETTERS & SETTERS ---

    /**
     * @return L'ID univoco del feedback.
     */
    public int getFeedbackId() { return feedbackId; }

    /**
     * @param feedbackId L'ID univoco da assegnare.
     */
    public void setFeedbackId(int feedbackId) { this.feedbackId = feedbackId; }

    /**
     * @return Il corpo testuale della valutazione.
     */
    public String getComment() { return comment; }

    /**
     * @param comment Il testo della valutazione.
     */
    public void setComment(String comment) { this.comment = comment; }

    /**
     * @return La data di emissione del feedback.
     */
    public LocalDateTime getDate() { return date; }

    /**
     * @param date La data da impostare.
     */
    public void setDate(LocalDateTime date) { this.date = date; }

    /**
     * @return L'ID del giudice associato.
     */
    public int getJudgeId() { return judgeId; }

    /**
     * @param judgeId L'ID del giudice da registrare.
     */
    public void setJudgeId(int judgeId) { this.judgeId = judgeId; }

    /**
     * @return L'ID del documento valutato.
     */
    public int getDocumentId() { return documentId; }

    /**
     * @param documentId L'ID del documento da associare.
     */
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    /**
     * @return Il nome del giudice (aggregato in fase di fetch).
     */
    public String getJudgeName() { return judgeName; }

    /**
     * @param judgeName Il nome del giudice da associare all'istanza.
     */
    public void setJudgeName(String judgeName) { this.judgeName = judgeName; }

    /**
     * Restituisce una rappresentazione in formato stringa dell'oggetto,
     * utile esclusivamente a fini di log o debug.
     *
     * @return La stringa formattata contenente i dati principali.
     */
    @Override
    public String toString() {
        return "Feedback [id=" + feedbackId + ", comment=" + comment + "]";
    }
}