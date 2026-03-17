package model;

/**
 * Rappresenta l'entità di dominio Giudice (Judge).
 * Questa classe estende l'entità base {@link User} per modellarne il ruolo specifico
 * all'interno di un evento Hackathon.
 * <p>
 * Nota Architetturale: Nel rigoroso rispetto del pattern BCE, questa classe
 * appartiene esclusivamente al layer Entity. È un puro POJO (Plain Old Java Object)
 * e non contiene alcuna logica di validazione, di business o di accesso al database.
 * L'ereditarietà viene utilizzata strategicamente per mappare i concetti di dominio
 * (Role-Based Access Control) senza duplicare i campi anagrafici e le credenziali.
 */
public class Judge extends User {

    private int hackathonId;

    /**
     * Costruttore vuoto di default.
     * Indispensabile per garantire la compatibilità con l'istanziazione dinamica
     * (reflection) da parte del layer DAO o eventuali framework di mappatura.
     */
    public Judge() {
        super();
    }

    /**
     * Costruttore completo per l'inizializzazione dell'entità Giudice.
     *
     * @param userId      L'ID univoco dell'utente (ereditato dalla superclasse).
     * @param name        Il nome completo o l'username (ereditato dalla superclasse).
     * @param email       L'indirizzo email di contatto (ereditato dalla superclasse).
     * @param password    La password per l'autenticazione (ereditata dalla superclasse).
     * @param hackathonId L'ID dell'hackathon a cui il giudice è ufficialmente assegnato per le valutazioni.
     */
    public Judge(int userId, String name, String email, String password, int hackathonId) {
        super(userId, name, email, password);
        setHackathonId(hackathonId);
    }

    /**
     * Recupera l'identificativo dell'hackathon supervisionato dal giudice.
     *
     * @return L'ID dell'hackathon associato.
     */
    public int getHackathonId() {
        return hackathonId;
    }

    /**
     * Assegna il giudice a un hackathon specifico.
     *
     * @param hackathonId L'ID dell'hackathon da assegnare.
     */
    public void setHackathonId(int hackathonId) {
        if (hackathonId <= 0) {
            throw new IllegalArgumentException("Hackathon ID must be greater than zero.");
        }
        this.hackathonId = hackathonId;
    }
}