package model;

/**
 * Rappresenta l'entità di dominio Partecipante (Participant).
 * Questa classe specializza l'entità {@link User} aggiungendo le informazioni
 * relative all'appartenenza a un team durante lo svolgimento dell'hackathon.
 * <p>
 * Nota Architetturale: All'interno del layer Entity (BCE), questa classe modella
 * un utente che ha completato l'iscrizione a un team specifico. La presenza di
 * {@code teamId} permette al sistema di filtrare i documenti e i feedback
 * visibili all'utente in base al principio di segregazione dei dati.
 */
public class Participant extends User {

    private int teamId;


    /**
     * Costruttore vuoto di default.
     * Necessario per garantire la compatibilità con i framework di persistenza
     * e per l'istanziazione dinamica nel layer DAO.
     */
    public Participant() {
        super();
    }

    /**
     * Costruttore completo per l'inizializzazione del partecipante.
     * Utilizzato solitamente dal layer Control o DAO durante la risoluzione dei ruoli utente.
     *
     * @param id       L'identificativo univoco dell'utente (ereditato da User).
     * @param name     Il nome visualizzato dell'utente (ereditato da User).
     * @param email    L'indirizzo email di registrazione (ereditato da User).
     * @param password La credenziale di accesso (ereditata da User).
     * @param teamId   L'identificativo del team a cui l'utente appartiene.
     */
    public Participant(int id, String name, String email, String password, int teamId) {
        super(id, name, email, password);
        setTeamId(teamId);
    }

    // --- GETTER & SETTER ---

    /**
     * Recupera l'ID del team associato al partecipante.
     *
     * @return L'ID del team.
     */
    public int getTeamId() {
        return teamId;
    }

    /**
     * Associa il partecipante a un team specifico.
     *
     * @param teamId L'identificativo del team da assegnare.
     */
    public void setTeamId(int teamId) {
        if (teamId <= 0) {
            throw new IllegalArgumentException("Team ID must be greater than zero.");
        }
        this.teamId = teamId;
    }


}