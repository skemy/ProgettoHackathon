package model;

/**
 * Rappresenta l'entità di dominio Utente (User) di base.
 * Questa classe funge da superclasse per tutti i ruoli del sistema (Partecipante, Giudice, Organizzatore).
 * <p>
 * Nota Architetturale: All'interno del pattern BCE, questa classe appartiene al layer Entity.
 * Modella i dati anagrafici e le credenziali di accesso.
 * Avviso: Poiché il DB di riferimento è PostgreSQL, il nome tabella "user" è una parola riservata.
 * Si raccomanda al layer DAO di referenziare la tabella come "users" per evitare conflitti sintattici.
 */
public class User {
    private int userId;
    private String name;
    private String email;
    private String password;

    /**
     * Costruttore vuoto di default.
     * Indispensabile per i processi di reflection e per l'inizializzazione
     * pigra (lazy loading) all'interno del layer DAO.
     */
    public User (){}

    /**
     * Costruttore completo per l'inizializzazione dell'entità con tutti i parametri.
     * Utilizzato per ricostruire l'oggetto dai dati provenienti dal database.
     *
     * @param userId   L'identificativo univoco dell'utente nel database.
     * @param name     Il nome completo o l'username dell'utente.
     * @param email    L'indirizzo email, utilizzato anche come identificativo per il login.
     * @param password La password dell'utente (memorizzata come hash nel sistema reale).
     */
    public User(int userId, String name, String email, String password) {
        setUserId(userId);
        setName(name);
        setEmail(email);
        setPassword(password);
    }

    /**
     * Recupera l'identificativo univoco dell'utente.
     * @return L'ID numerico dell'utente.
     */
    public int getUserId() { return userId; }

    /**
     * Imposta l'identificativo univoco dell'utente.
     * @param userId L'ID da assegnare.
     */
    public void setUserId(int userId) {
        if (userId < 0) throw new IllegalArgumentException("User ID cannot be negative.");
        this.userId = userId;
    }

    /**
     * Recupera il nome dell'utente.
     * @return Il nome testuale dell'utente.
     */
    public String getName() { return name; }

    /**
     * Imposta il nome dell'utente.
     * @param name Il nome da assegnare.
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be null or empty.");
        this.name = name;
    }

    /**
     * Recupera l'indirizzo email dell'utente.
     * @return L'email dell'utente.
     */
    public String getEmail() { return email; }

    /**
     * Imposta l'indirizzo email dell'utente.
     * @param email L'email da assegnare.
     */
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        this.email = email;
    }

    /**
     * Recupera la password (hash) dell'utente.
     * @return La stringa della password.
     */
    public String getPassword() { return password; }

    /**
     * Imposta la password dell'utente.
     * @param password La password da assegnare.
     */
    public void setPassword(String password) {
        if (password == null || password.trim().isEmpty()) throw new IllegalArgumentException("Password cannot be null or empty.");
        this.password = password;
    }

    /**
     * Fornisce una rappresentazione testuale dell'oggetto.
     * Utilizzata esclusivamente per attività di logging tecnico o debugging del layer Control.
     * * @return Una stringa contenente i metadati principali dell'utente.
     */
    @Override
    public String toString() {
        return "User [id=" + userId + ", name=" + name + ", email=" + email + "]";
    }
}