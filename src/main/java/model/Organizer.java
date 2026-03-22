package model;


/**
 * Rappresenta l'entità di dominio Organizzatore (Organizer).
 * Estende la classe {@link User} per includere le responsabilità specifiche
 * legate alla gestione e configurazione di un evento Hackathon.
 * <p>
 * Nota Architetturale: All'interno del pattern BCE, questa classe funge da Entity.
 * La specializzazione di {@code User} in {@code Organizer} permette al sistema
 * di implementare un controllo degli accessi basato sui ruoli (RBAC), legando
 * univocamente un utente a un'istanza di Hackathon con privilegi di amministrazione.
 */
public class Organizer extends User {

    private int hackathonId;

    /**
     * Costruttore vuoto necessario per i processi di serializzazione
     * e per l'istanziazione tramite reflection nel layer DAO.
     */
    public Organizer() {
        super();
    }

    /**
     * Costruttore completo per inizializzare un organizzatore con i dati utente e l'evento associato.
     *
     * @param userId      L'identificativo univoco dell'utente.
     * @param name        Il nome o lo username dell'organizzatore.
     * @param email       L'indirizzo email di riferimento.
     * @param password    La stringa di autenticazione (hash).
     * @param hackathonId L'identificativo dell'hackathon creato o gestito dall'utente.
     */
    public Organizer(int userId, String name, String email, String password, int hackathonId) {
        super(userId, name, email, password);
        setHackathonId(hackathonId);
    }

    /**
     * Recupera l'ID dell'hackathon sotto la gestione di questo organizzatore.
     *
     * @return L'identificativo numerico dell'hackathon.
     */
    public int getHackathonId() {
        return hackathonId;
    }

    @Override
    public int getAssociatedHackathonId() {
        return this.hackathonId;
    }

    @Override
    public String getTeamActionDenialReason() {
        return "You are an Organizer. You cannot create or join a team.";
    }

    /**
     * Associa l'organizzatore a un hackathon specifico.
     *
     * @param hackathonId L'ID dell'evento da assegnare.
     */
    public void setHackathonId(int hackathonId) {
        if (hackathonId <= 0) {
            throw new IllegalArgumentException("Hackathon ID must be greater than zero.");
        }
        this.hackathonId = hackathonId;
    }
}