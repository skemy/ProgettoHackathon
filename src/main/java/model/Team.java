package model;

import java.time.LocalDateTime;

/**
 * Rappresenta l'entità di dominio Team all'interno del sistema.
 * Questa classe modella un gruppo di partecipanti iscritti a un determinato Hackathon.
 * <p>
 * Nota Architetturale: In quanto componente del layer Entity (pattern BCE), questa classe
 * è un puro contenitore di dati (POJO). Non contiene logica di generazione dei codici
 * o validazione, compiti delegati rispettivamente al layer DAO (per la persistenza)
 * e al Controller (per l'orchestrazione della business logic).
 */
public class Team {

    private int teamId;
    private String teamName;
    private String accessCode;
    private LocalDateTime creationDate;
    private int hackathonId;

    /**
     * Costruttore vuoto di default.
     * Necessario per supportare la creazione dell'istanza tramite reflection nel layer DAO
     * e per garantire la compatibilità con i framework di persistenza.
     */
    public Team() {}

    /**
     * Costruttore completo per l'inizializzazione dell'entità Team.
     *
     * @param teamId       L'identificativo univoco del team.
     * @param teamName     Il nome assegnato al gruppo.
     * @param accessCode   Il codice univoco per permettere ad altri utenti di unirsi al team.
     * @param creationDate Il timestamp relativo alla creazione del team.
     * @param hackathonId  L'identificativo dell'hackathon di riferimento.
     */
    public Team(int teamId, String teamName, String accessCode, LocalDateTime creationDate, int hackathonId) {
        setTeamId(teamId);
        setTeamName(teamName);
        setAccessCode(accessCode);
        setCreationDate(creationDate);
        setHackathonId(hackathonId);
    }

    /**
     * Recupera l'ID univoco del team.
     * * @return L'identificativo numerico del team.
     */
    public int getTeamId() {
        return teamId;
    }

    /**
     * Definisce l'ID univoco del team.
     * * @param teamId L'ID da assegnare.
     */
    public void setTeamId(int teamId) {
        if (teamId < 0) throw new IllegalArgumentException("Team ID cannot be negative.");
        this.teamId = teamId;
    }

    /**
     * Recupera il nome del team.
     * * @return Il nome testuale del team.
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Imposta il nome del team.
     * * @param teamName Il nome da assegnare.
     */
    public void setTeamName(String teamName) {
        if (teamName == null || teamName.trim().isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty.");
        }
        this.teamName = teamName;
    }

    /**
     * Recupera il codice di accesso per l'adesione dei membri.
     * * @return Il codice alfanumerico di accesso.
     */
    public String getAccessCode() {
        return accessCode;
    }

    /**
     * Imposta il codice di accesso univoco.
     * * @param accessCode Il codice da assegnare.
     */
    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    /**
     * Recupera la data e l'ora di creazione del team.
     * * @return Un oggetto {@link LocalDateTime} con il timestamp di creazione.
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Imposta la data di creazione del team.
     * * @param creationDate Il timestamp da associare.
     */
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Recupera l'ID dell'hackathon associato al team.
     * * @return L'ID dell'hackathon.
     */
    public int getHackathonId() {
        return hackathonId;
    }

    /**
     * Associa il team a un hackathon specifico.
     * * @param hackathonId L'ID dell'hackathon di riferimento.
     */
    public void setHackathonId(int hackathonId) {
        if (hackathonId <= 0) throw new IllegalArgumentException("Hackathon ID must be greater than zero.");
        this.hackathonId = hackathonId;
    }
}