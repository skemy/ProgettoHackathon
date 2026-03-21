package controller;

import dao.*;
import exceptions.*;
import model.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;



/**
 * Orchestratore centrale del sistema (Layer Control nel pattern BCE - Boundary Control Entity).
 * Coordina il flusso di dati tra la Boundary (GUI) e l'Entity Access (DAO), incapsulando
 * la logica di business principale.
 * Viene gestito come fulcro per il mantenimento dello stato della sessione utente
 * corrente e per l'applicazione delle regole di autorizzazione.
 */
public class Controller {

    /** Logger per tracciare eventi critici, preferito al System.out per una gestione professionale degli stream di log. */
    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    private final HackathonDAO hackathonDAO;
    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final DocumentDAO documentDAO;
    private final VoteDAO voteDAO;
    private final FeedbackDAO feedbackDAO;

    /** * Mantiene lo stato della sessione attiva. Sfruttando il polimorfismo, conterrà l'istanza
     * specifica del ruolo (User, Participant, Judge, Organizer).
     */
    private User loggedInUser;

    /** * Flag di stato utile per la UI. Indica se l'utente è stato rimosso da un evento
     * (es. registrazione pendente ma evento iniziato) per triggerare feedback visivi.
     */
    private boolean recentlyKicked = false;

    /**
     * Costruttore di default.
     * Inizializza le implementazioni concrete dei DAO. Sebbene un approccio basato su
     * Dependency Injection (es. Spring) sarebbe più scalabile, l'inizializzazione diretta
     * qui è stata scelta per mantenere il framework-less design.
     */
    public Controller() {
        this.userDAO = new UserDAOImpl();
        this.hackathonDAO = new HackathonDAOImpl();
        this.teamDAO = new TeamDAOImpl();
        this.documentDAO = new DocumentDAOImpl();
        this.voteDAO = new VoteDAOImpl();
        this.feedbackDAO = new FeedbackDAOImpl();
    }

    // --- LOGICA DI SICUREZZA (EVENT TERMINATION) ---

    /**
     * Verifica proattiva per garantire che l'hackathon corrente sia ancora in corso.
     * Viene invocato prima di operazioni di mutazione (scrittura su DB) per impedire
     * modifiche a eventi conclusi, imponendo un soft-lock (sola lettura) a livello applicativo.
     * * @throws SQLException Se si verifica un errore durante il recupero dell'hackathon.
     * @throws IllegalStateException Se nessun hackathon è attivo o se l'evento è già terminato.
     */
    /* TODO: In produzione, aggiungere now.isBefore(current.getStartDate())
    *  per impedire azioni pre-evento. Mantenuto permissivo per scopi di demo/test.
    */

    private void ensureHackathonIsActive() throws SQLException {
        Hackathon current = getCurrentHackathon();
        if (current == null) {
            throw new IllegalStateException("No active hackathons found for this operation.");
        }

        if (LocalDateTime.now().isAfter(current.getEndDate())) {
            throw new IllegalStateException("The event ended on " +
                    current.getEndDate().toLocalDate() + ". The system is now in read-only mode.");
        }
    }

    // --- GESTIONE STATO UTENTE ---

    /**
     * Ritorna lo stato del flag di espulsione recente. Utile alla GUI per mostrare alert specifici.
     * @return true se l'utente è stato escluso di recente da un limbo, false altrimenti.
     */
    public boolean wasRecentlyKicked() { return recentlyKicked; }

    /**
     * Resetta il flag di espulsione dopo che la Boundary ha notificato l'utente.
     */
    public void resetKickedFlag() { this.recentlyKicked = false; }

    /**
     * Ritorna l'utente attualmente loggato. Restituisce il tipo base {@link User}, ma a runtime
     * sarà istanza del ruolo specifico assegnato.
     * @return L'utente in sessione.
     */
    public User getCurrentUser() { return this.loggedInUser; }

    /**
     * Gestisce l'azione di Login. Valida gli input primari, interroga il DB e, in caso di successo,
     * innesca la risoluzione dinamica del ruolo dell'utente per il polimorfismo.
     * * @param name L'username inserito.
     * @param password La password in chiaro inserita.
     * @throws BlankFieldException Se i campi sono nulli o vuoti, riducendo le query inutili al DB.
     * @throws UserNotFoundException Se le credenziali non trovano riscontro.
     * @throws SQLException In caso di errori di comunicazione con il database.
     */
    public void loginUserAction(String name, String password) throws UserNotFoundException, SQLException {
        if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BlankFieldException("Username and Password are required!");
        }

        User baseUser = userDAO.checkLogin(name, password);
        if (baseUser == null) {
            throw new UserNotFoundException("Incorrect credentials.");
        }

        this.loggedInUser = resolveActualUserRole(baseUser);
        LOGGER.log(Level.INFO, "Login authorized for: {0}", name);
    }

    /**
     * Cuore della logica polimorfica. Prende un utente base e interroga le tabelle relazionali
     * (team, giudici, organizzatori) per istanziare e restituire la sottoclasse corretta
     * (Participant, Judge, Organizer). Questo evita di avere flag booleani sparsi per capire "chi è chi".
     * * @param u L'utente base appena loggato.
     * @return L'utente decorato con il suo ruolo specifico.
     * @throws SQLException In caso di problemi durante il controllo dei ruoli sul DB.
     */
    private User resolveActualUserRole(User u) throws SQLException {
        int uid = u.getUserId();
        int tId = teamDAO.getTeamIdByUserId(uid);

        if (tId > 0) {
            int hId = teamDAO.getHackathonIdByTeam(tId);
            return new Participant(uid, u.getName(), u.getEmail(), u.getPassword(), tId, hId);
        }

        int judgeHid = userDAO.getHackathonIdWhereUserIsJudge(uid);
        if (judgeHid > 0) return new Judge(uid, u.getName(), u.getEmail(), u.getPassword(), judgeHid);

        int orgHid = hackathonDAO.getHackathonIdWhereUserIsOrganizer(uid);
        if (orgHid > 0) return new Organizer(uid, u.getName(), u.getEmail(), u.getPassword(), orgHid);

        return u;
    }

    /**
     * Coordina la registrazione di un nuovo account. Centralizza i controlli di unicità (username/email)
     * prima di procedere con l'inserimento nel persistence layer.
     * * @param username Il nome utente scelto.
     * @param email L'email fornita.
     * @param password La password per il nuovo account.
     * @throws BlankFieldException Se mancano parametri obbligatori.
     * @throws UsernameAlreadyTakenException Se il sistema rileva un conflitto sull'username.
     * @throws EmailAlreadyTakenException Se il sistema rileva un conflitto sull'email.
     * @throws SQLException Per errori legati al DB.
     */
    public void registerUserAction(String username, String email, String password)
            throws UsernameAlreadyTakenException, EmailAlreadyTakenException, SQLException {
        if (username == null || email == null || password == null) throw new BlankFieldException("Mandatory fields missing.");
        if (userDAO.isUsernameAlreadyRegistered(username)) throw new UsernameAlreadyTakenException("Username used.");
        if (userDAO.isEmailAlreadyRegistered(email)) throw new EmailAlreadyTakenException("Email used.");

        userDAO.registerUser(new User(0, username, email, password));
    }

    // --- AZIONI HACKATHON ---

    /**
     * Recupera l'elenco di tutti gli hackathon presenti nel sistema.
     * @return Una lista di oggetti Hackathon.
     * @throws SQLException In caso di errori SQL.
     */
    public List<Hackathon> getAllHackathons() throws SQLException {
        return hackathonDAO.getAllHackathons();
    }

    /**
     * Gestisce l'iscrizione di un utente a un hackathon, verificandone i requisiti di accesso.
     * <p>
     * Il metodo esegue una serie di controlli sequenziali:
     * 1. Verifica che l'evento non sia già iniziato (controllo temporale).
     * 2. Verifica che l'evento non abbia raggiunto il numero massimo di partecipanti
     * consentito (Sold Out).
     * 3. Verifica che l'utente non ricopra già ruoli attivi (Organizer, Judge, Participant)
     * in altri eventi.
     * 4. Verifica che l'utente non abbia già una registrazione pendente nel "limbo".
     * </p>
     *
     * @param hackathonId L'identificativo univoco dell'hackathon a cui iscriversi.
     * @throws CannotRegisterToEventException Se l'evento è iniziato, se è stato raggiunto il limite
     * massimo di iscritti o se l'utente ha già un ruolo attivo.
     * @throws SQLException In caso di errori durante l'interrogazione del database
     * (es. conteggio partecipanti o recupero hackathon).
     */
    public void joinHackathonAction(int hackathonId) throws CannotRegisterToEventException, SQLException {
        Hackathon target = hackathonDAO.getHackathonById(hackathonId);

        // Controllo Temporale (Evento chiuso)
        if (target != null && LocalDateTime.now().isAfter(target.getStartDate())) {
            throw new CannotRegisterToEventException("The event is already closed for registrations.");
        }
        if (target != null) {

            int currentTotalParticipants = userDAO.countTotalParticipantsByHackathon(hackathonId);
            if (currentTotalParticipants >= target.getMaxParticipants()) {
                throw new CannotRegisterToEventException("Sold Out! This hackathon reached the limit of " +
                        target.getMaxParticipants() + " participants.");
            }
        }
        // Controllo dei Ruoli (Organizer, Judge, Participant)
        if (!loggedInUser.getClass().equals(User.class)) {
            if (loggedInUser instanceof Organizer) {
                throw new CannotRegisterToEventException("You are an organizer of an event. You can't participate to another Hackathon.");
            } else {
                throw new CannotRegisterToEventException("You already have an active role in another event.");
            }
        }

        // Controllo Limbo (Utente base già iscritto altrove)
        if (userDAO.getRegisteredHackathonId(loggedInUser.getUserId()) > 0) {
            throw new CannotRegisterToEventException("You are already registered to another hackathon.");
        }

        // Se passa tutti i controlli, esegue l'iscrizione
        userDAO.registerUserToHackathon(loggedInUser.getUserId(), hackathonId);
    }

    /**
     * Risolve dinamicamente l'hackathon associato all'utente loggato.
     * Integra la logica di "pulizia" (cleanupLimboRegistrations) nel caso in cui l'evento sia iniziato
     * e l'utente non abbia finalizzato l'ingresso in un team, gestendo l'espulsione automatica.
     * * @return L'Hackathon di pertinenza dell'utente, oppure null se non associato o espulso.
     * @throws SQLException In caso di errori relazionali o di query.
     */
    public Hackathon getCurrentHackathon() throws SQLException {
        if (loggedInUser == null) return null;
        int hId = resolveCurrentHackathonId();
        if (hId <= 0) return null;

        Hackathon h = hackathonDAO.getHackathonById(hId);
        if (h != null && !LocalDateTime.now().isBefore(h.getStartDate())) {
            userDAO.cleanupLimboRegistrations(hId);
            if (loggedInUser.getClass().equals(User.class)) {
                this.recentlyKicked = true;
                return null;
            }
        }
        return h;
    }

    /**
     * Metodo helper privato che sfrutta il dynamic binding.
     * Se l'utente ha un ruolo specifico (getAssociatedHackathonId ovverridato), ottiene l'ID direttamente
     * dalla ram (modello). Altrimenti (se è nel Limbo o Base), esegue una query al DB.
     * * @return L'ID dell'Hackathon associato all'utente, 0 se non trovato.
     * @throws SQLException In caso di problemi sul database nel branch di fallback.
     */
    private int resolveCurrentHackathonId() throws SQLException {
        if (loggedInUser == null) return 0;

        // Sfruttiamo il polimorfismo del Model per ottenere l'ID
        int id = loggedInUser.getAssociatedHackathonId();

        // Se è 0 (utente nel limbo), cerchiamo l'ID dell'iscrizione pendente nel DB
        return (id > 0) ? id : userDAO.getRegisteredHackathonId(loggedInUser.getUserId());
    }

    /**
     * Coordina la creazione di un nuovo Hackathon.
     * Sfrutta il Builder Pattern per assemblare il dominio in modo leggibile (evitando il telescoping constructor).
     * Gestisce atomicamente l'inserimento dell'evento e la promozione dell'utente creatore a Organizer.
     * * @param title Titolo dell'evento.
     * @param location Luogo fisico o virtuale.
     * @param startDate Data d'inizio dell'evento.
     * @param endDate Data di fine dell'evento.
     * @param maxP Limite massimo partecipanti.
     * @param maxT Limite massimo della grandezza team.
     * @throws SQLException In caso di fallimento della persistenza o dell'aggiornamento ruoli.
     */
    public void createHackathonAction(String title, String location, LocalDate startDate, LocalDate endDate, int maxP, int maxT) throws SQLException, BlankFieldException{
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59);

        Hackathon newEvent = new Hackathon.Builder()
                .title(title).location(location)
                .startDate(start).endDate(end)
                .registrationStartDate(start.minusDays(2))
                .registrationEndDate(start.minusDays(1))
                .maxParticipants(maxP).maxTeamSize(maxT).build();

        hackathonDAO.createHackathon(newEvent);
        userDAO.promoteToOrganizer(loggedInUser.getUserId(), newEvent.getHackathonId());
        this.loggedInUser = resolveActualUserRole(loggedInUser);
    }

    /**
     * Aggiorna la descrizione del problema di un hackathon in corso.
     * Invoca `ensureHackathonIsActive()` per garantire che non vengano alterati eventi storici.
     * * @param description Il nuovo testo della problematica.
     * @return true se l'aggiornamento è avvenuto con successo, false se l'hackathon non è valido.
     * @throws SQLException Per problemi di update sul DB.
     */
    public boolean updateHackathonProblemAction(String description) throws SQLException {
        ensureHackathonIsActive();
        Hackathon current = getCurrentHackathon();
        if (current == null) return false;
        hackathonDAO.updateProblemDescription(current.getHackathonId(), description);
        return true;
    }

    // --- AZIONI TEAM E DOCUMENTI ---

    /**
     * Crea un nuovo team ed estrae l'utente dal "limbo" delle iscrizioni pendenti.
     * Rialloca l'utente elevandone lo stato a `Participant` forzando una ri-risoluzione del ruolo.
     * * @param teamName Il nome desiderato per il team.
     * @return Il codice d'accesso generato per il nuovo team.
     * @throws SQLException Se vi sono errori nelle diverse fasi della transazione (creazione, link, rimozione limbo).
     * @throws IllegalStateException Se l'utente tenta di creare un team senza aver prima scelto un evento.
     * @throws IllegalArgumentException Se si verifica un errore anomalo durante la generazione dell'ID del team.
     */
    public String createTeamAction(String teamName) throws SQLException,BlankFieldException {
        ensureHackathonIsActive();
        int hId = userDAO.getRegisteredHackathonId(loggedInUser.getUserId());
        if (hId <= 0) throw new IllegalStateException("Register for an event first.");

        Team newTeam = new Team(0, teamName, "", null, hId);
        int teamId = teamDAO.createTeamAndReturnId(newTeam);
        if (teamId <= 0) throw new IllegalArgumentException("Team creation error.");

        teamDAO.linkUserToTeam(loggedInUser.getUserId(), teamId);
        userDAO.removeFromLimbo(loggedInUser.getUserId(), hId);
        this.loggedInUser = resolveActualUserRole(loggedInUser);
        return newTeam.getAccessCode();
    }

    /**
     * Associa un utente in stato "limbo" a un team preesistente utilizzando un codice segreto.
     * * @param accessCode Il codice invito rilasciato dal fondatore del team.
     * @throws SQLException Se si fallisce il collegamento o la cancellazione dal limbo.
     * @throws IllegalArgumentException Se il codice inserito non corrisponde a nessun team attivo.
     */
    /**
     * Associa un utente in stato "limbo" a un team preesistente utilizzando un codice segreto.
     * @param accessCode Il codice invito rilasciato dal fondatore del team.
     * @throws SQLException Se si fallisce il collegamento o la cancellazione dal limbo.
     * @throws IllegalArgumentException Se il codice non corrisponde o se il team è pieno.
     */
    /**
     * Associa un utente in stato "limbo" a un team preesistente utilizzando un codice segreto.
     * @param accessCode Il codice invito rilasciato dal fondatore del team.
     * @throws SQLException Se si fallisce il collegamento o la cancellazione dal limbo.
     * @throws IllegalArgumentException Se il codice non è valido o se il team è pieno.
     */
    public void joinTeamAction(String accessCode) throws SQLException ,BlankFieldException{
        ensureHackathonIsActive();

        int teamId = teamDAO.getTeamIdByCode(accessCode);

        if (teamId <= 0) {
            throw new IllegalArgumentException("Invalid Code");
        }
        int teamHid = teamDAO.getHackathonIdByTeam(teamId);
        Hackathon targetHackathon = hackathonDAO.getHackathonById(teamHid);

        if (targetHackathon != null) {
            int maxSize = targetHackathon.getMaxTeamSize();
            int currentMembers = teamDAO.getTeamMembers(teamId).size();
            if (currentMembers >= maxSize) {
                throw new IllegalArgumentException("The team is full! Maximum capacity is " + maxSize + " members.");
            }
        }

        int registeredHid = userDAO.getRegisteredHackathonId(loggedInUser.getUserId());
        teamDAO.linkUserToTeam(loggedInUser.getUserId(), teamId);

        // Rimuove l'utente dal limbo
        userDAO.removeFromLimbo(loggedInUser.getUserId(), registeredHid);

        // Aggiorna la sessione (l'utente diventa Participant)
        this.loggedInUser = resolveActualUserRole(loggedInUser);
    }

    /**
     * Aggiunge un documento/progetto per il team dell'utente loggato.
     * Viene utilizzato `instanceof` esplicitamente come barriera di autorizzazione (RBAC a livello codice),
     * garantendo che un Organizer/Judge o semplice User non possa falsificare l'upload di progetti.
     * * @param name Nome formale del documento.
     * @param url Link di riferimento (es. repository GitHub o PDF).
     * @throws SQLException In caso di problemi col DB.
     * @throws IllegalStateException Se chi chiama l'azione non detiene il ruolo di `Participant`.
     */
    public void addDocumentAction(String name, String url) throws SQLException {
        ensureHackathonIsActive();
        if (!(loggedInUser instanceof Participant p)) throw new IllegalStateException("Only participants can upload projects.");
        documentDAO.uploadDocument(new Document(0, name, url, LocalDateTime.now(), p.getTeamId(), resolveCurrentHackathonId()));
    }

    // --- AZIONI GIUDICI E VOTI ---

    /**
     * Eleva un utente standard al ruolo di Giudice per l'Hackathon corrente.
     * * @param userId L'ID dell'utente da promuovere.
     * @throws SQLException Se si verifica un errore durante l'aggiornamento dei permessi.
     */
    public void promoteToJudgeAction(int userId) throws SQLException {
        ensureHackathonIsActive();
        userDAO.promoteToJudge(userId, getCurrentHackathon().getHackathonId());
    }

    /**
     * Registra il voto assegnato da un giudice a un team. Passa per la validazione temporale
     * di `ensureHackathonIsActive()`.
     * * @param teamId L'ID del team giudicato.
     * @param score Il punteggio (generalmente intero, da 1 a X).
     * @return true se l'inserimento va a buon fine.
     * @throws SQLException Se si viola qualche constraint sul database.
     */
    public boolean voteTeamAction(int teamId, float score) throws SQLException {
        ensureHackathonIsActive();
        return voteDAO.insertVote(loggedInUser.getUserId(), teamId, score);
    }

    /**
     * Salva o aggiorna il feedback testuale di un giudice su uno specifico documento.
     * Permette ai giudici di fornire motivazioni per i loro voti (implementazione dell'UPSERT).
     * * @param documentId L'ID del documento recensito.
     * @param text Il corpo del feedback.
     * @throws SQLException Per errori su database.
     * @throws BlankFieldException Se il feedback inviato risulta vuoto o inesistente.
     */
    public void saveFeedbackAction(int documentId, String text) throws SQLException, BlankFieldException {
        ensureHackathonIsActive();
        feedbackDAO.saveOrUpdateFeedback(loggedInUser.getUserId(), documentId, text);
    }

    // --- METODI DI LETTURA E CLASSIFICHE ---

    /**
     * Restituisce la lista di utenti iscritti all'evento ma che non hanno ancora fondato/joinato un team.
     * @return Lista di utenti in "limbo".
     * @throws SQLException In caso di query failed.
     */
    public List<User> getUsersInLimbo() throws SQLException {
        Hackathon h = getCurrentHackathon();
        return (h == null) ? new ArrayList<>() : userDAO.getUsersInLimboByHackathon(h.getHackathonId());
    }

    /**
     * Calcola e recupera la classifica finale dell'hackathon.
     * Per design, la classifica "finale" non viene mai rivelata prima della chiusura formale dell'evento a tutti i partecipanti, ma resta possibile la visualizzazione all'organizzatore.
     * per evitare comportamenti strategici scorretti.
     * * @return Lista delle metriche/voti aggregati in formato stringa.
     * @throws SQLException In caso di problemi col modulo VoteDAO.
     * @throws IllegalStateException Se l'evento è ancora in corso.
     */
    public List<String> getFinalRanking() throws SQLException {
        Hackathon h = getCurrentHackathon();
        if (h == null || LocalDateTime.now().isBefore(h.getEndDate())) throw new IllegalStateException("Event in progress. Ranking unavailable.");
        return voteDAO.getLeaderboard(h.getHackathonId());
    }

    /**
     * Consente esclusivamente agli organizzatori di bypassare il blocco temporale per
     * monitorare l'andamento delle votazioni dal vivo.
     * * @return Lista formattata delle classifiche parziali.
     * @throws SQLException In caso di problemi col modulo VoteDAO.
     */
    public List<String> getLiveRankingForOrganizer() throws SQLException {
        return voteDAO.getLeaderboard(getCurrentHackathon().getHackathonId());
    }

    /**
     * Ottiene l'elenco dei membri del team dell'utente corrente tramite downcasting sicuro.
     * @return La lista dei `Participant` nello stesso team, vuota se l'utente non è in un team.
     * @throws SQLException Per errori durante il retrieve dal DAO.
     */
    public List<Participant> getMyTeamMembers() throws SQLException {
        return (loggedInUser instanceof Participant p) ? teamDAO.getTeamMembers(p.getTeamId()) : new ArrayList<>();
    }

    /**
     * Ottiene l'elenco dei documenti/progetti caricati dal team dell'utente corrente.
     * @return La lista dei `Document` posseduti dal team.
     * @throws SQLException Per errori SQL.
     */
    public List<Document> getMyTeamDocuments() throws SQLException {
        return (loggedInUser instanceof Participant p) ? documentDAO.getDocumentsByTeam(p.getTeamId()) : new ArrayList<>();
    }

    /**
     * Recupera le informazioni del team dell'utente connesso (es. nome e access code).
     * @return Il `Team` associato, o null se l'utente non è un partecipante.
     * @throws SQLException In caso di errori di comunicazione con il DAO.
     */
    public Team getMyTeam() throws SQLException {
        return (loggedInUser instanceof Participant p) ? teamDAO.getTeamById(p.getTeamId()) : null;
    }

    /**
     * Restituisce tutti i team iscritti e operativi per l'Hackathon di riferimento.
     * @return Lista totale dei team per l'evento.
     * @throws SQLException Per errori su database.
     */
    public List<Team> getTeamsByHackathon() throws SQLException {
        int hId = resolveCurrentHackathonId();
        return hId > 0 ? teamDAO.getTeamsByHackathon(hId) : new ArrayList<>();
    }

    /**
     * @param teamId ID del team in esame.
     * @return Membri di un team specifico.
     * @throws SQLException Per errori di lettura.
     */
    public List<Participant> getTeamMembers(int teamId) throws SQLException, BlankFieldException { return teamDAO.getTeamMembers(teamId); }

    /**
     * @param teamId ID del team in esame.
     * @return Documentazione caricata dal team.
     * @throws SQLException Per errori di lettura.
     */
    public List<Document> getTeamDocuments(int teamId) throws SQLException { return documentDAO.getDocumentsByTeam(teamId); }

    /**
     * Recupera l'elenco dei commenti (feedback) rilasciati dai giudici per uno specifico documento.
     * @param documentId L'ID del documento analizzato.
     * @return Lista dei `Feedback` raccolti.
     * @throws SQLException Per errori SQL.
     */
    public List<Feedback> getDocumentFeedbacks(int documentId) throws SQLException { return feedbackDAO.getAllFeedbacksForDocument(documentId); }

    /**
     * Legge lo specifico feedback rilasciato in precedenza dall'utente corrente su un documento.
     * Utilizzato per popolare campi di edit qualora il giudice voglia modificare una recensione.
     * * @param documentId Il documento per cui cercare il feedback.
     * @return Il testo salvato, o una stringa vuota se assente.
     * @throws SQLException Per errori di accesso al DB.
     */
    public String getMyFeedbackForDocument(int documentId) throws SQLException {
        return loggedInUser != null ? feedbackDAO.getFeedbackText(loggedInUser.getUserId(), documentId) : "";
    }

    /**
     * Controlla lo stato di voto del giudice per bloccare i multi-voto da GUI.
     * @param teamId Il team da verificare.
     * @return true se il giudice ha già espresso una valutazione per quel team.
     * @throws SQLException In caso di query fallita.
     */
    public boolean hasJudgeAlreadyVoted(int teamId) throws SQLException {
        return loggedInUser != null && voteDAO.checkIfAlreadyVoted(loggedInUser.getUserId(), teamId);
    }

    /**
     * Ottiene l'username del creatore dell'evento.
     * @param id L'ID dell'hackathon.
     * @return Nome in chiaro dell'Organizzatore.
     * @throws SQLException Per eccezioni SQL.
     */
    public String getOrganizerNameForHackathon(int id) throws SQLException { return hackathonDAO.getOrganizerUsernameByHackathonId(id); }

    /**
     * Helper di autorizzazione basato sui ruoli. Sfrutta il polimorfismo instanziato nel login.
     * @return true se l'utente in sessione è un Organizzatore.
     */
    public boolean isCurrentUserOrganizer() { return loggedInUser instanceof Organizer; }

    /**
     * Helper di autorizzazione basato sui ruoli.
     * @return true se l'utente in sessione ricopre il ruolo di Giudice.
     */
    public boolean isCurrentUserJudge() { return loggedInUser instanceof Judge; }

    /**
     * Verifica se l'utente ha i privilegi base (User non ancora iscritto ad alcun evento)
     * e pertanto ha facoltà di fondare un nuovo Hackathon da zero diventandone organizzatore.
     * @return true se è uno User puro.
     */
    public boolean canUserCreateHackathon() { return loggedInUser != null && loggedInUser.getClass().equals(User.class); }

    /**
     * Valida tramite polimorfismo se l'utente attuale può gestire team.
     * @throws IllegalStateException se l'utente non ha i permessi.
     */
    public void validateTeamManagementAccess() throws IllegalStateException {
        if (loggedInUser == null) throw new IllegalStateException("User not logged in.");
        String denialReason = loggedInUser.getTeamActionDenialReason();
        if (denialReason != null) {
            throw new IllegalStateException(denialReason);
        }
    }

}
