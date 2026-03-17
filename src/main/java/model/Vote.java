package model;

/**
 * Rappresenta l'entità di dominio Voto (Vote).
 * Modella la valutazione numerica espressa da un giudice nei confronti di un team specifico.
 * <p>
 * Nota Architetturale: All'interno del pattern BCE, questa classe funge da Entity.
 * A livello di persistenza (PostgreSQL), questa entità è mappata su una tabella
 * che utilizza una chiave primaria auto-incrementante (voteId) e un vincolo di
 * unicità (UNIQUE) sulle colonne {@code judgeId} e {@code teamId}.
 */
public class Vote {
    private int voteId;
    private int judgeId;
    private int teamId;
    private int score;

    /**
     * Costruttore vuoto di default.
     * Necessario per le operazioni di mappatura del layer DAO e per i processi
     * di istanziazione tramite reflection o framework di persistenza.
     */
    public Vote() {}

    /**
     * Costruttore completo per l'inizializzazione di un voto recuperato dal database.
     *
     * @param voteId  L'identificativo univoco (PK) del record nella tabella vote.
     * @param judgeId L'identificativo univoco del giudice che esprime il voto.
     * @param teamId  L'identificativo univoco del team che riceve la valutazione.
     * @param score   Il punteggio assegnato (solitamente in un range 1-10).
     */
    public Vote(int voteId, int judgeId, int teamId, int score) {
        setVoteId(voteId);
        setJudgeId(judgeId);
        setTeamId(teamId);
        setScore(score);
    }

    /**
     * Recupera l'ID univoco del voto.
     * @return L'ID primario del voto.
     */
    public int getVoteId() { return voteId; }

    /**
     * Imposta l'ID univoco del voto.
     * @param voteId L'identificativo da assegnare.
     */
    public void setVoteId(int voteId) {
        if (voteId < 0) throw new IllegalArgumentException("Vote ID cannot be negative.");
        this.voteId = voteId;
    }

    /**
     * Recupera l'ID del giudice autore della valutazione.
     * @return L'ID del giudice.
     */
    public int getJudgeId() { return judgeId; }

    /**
     * Imposta l'ID del giudice autore della valutazione.
     * @param judgeId L'identificativo del giudice da associare.
     */
    public void setJudgeId(int judgeId) {
        if (judgeId <= 0) throw new IllegalArgumentException("Judge ID must be greater than zero.");
        this.judgeId = judgeId;
    }

    /**
     * Recupera l'ID del team valutato.
     * @return L'ID del team.
     */
    public int getTeamId() { return teamId; }

    /**
     * Imposta l'ID del team da valutare.
     * @param teamId L'identificativo del team da associare.
     */
    public void setTeamId(int teamId) {
        if (teamId <= 0) throw new IllegalArgumentException("Team ID must be greater than zero.");
        this.teamId = teamId;
    }

    /**
     * Recupera il punteggio assegnato.
     * @return Il valore numerico del voto.
     */
    public int getScore() { return score; }

    /**
     * Definisce il punteggio della valutazione.
     * Garantisce l'integrità dei dati eliminando la Primitive Obsession per questo parametro.
     * @param score Il punteggio da assegnare.
     */
    public void setScore(int score) {
        if (score < 0 || score > 10) throw new IllegalArgumentException("Score must be between 0 and 10.");
        this.score = score;
    }
}