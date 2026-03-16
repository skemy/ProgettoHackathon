package controller;

import dao.*;
import exceptions.BlankFieldException;
import exceptions.EmailAlreadyTakenException;
import exceptions.UserNotFoundException;
import exceptions.UsernameAlreadyTakenException;
import model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Orchestratore centrale del sistema (Layer Control nel pattern BCE).
 * Coordina il flusso di dati tra la Boundary (GUI) e l'Entity Access (DAO).
 * <p>
 * Nota Architetturale: Seguendo il principio di Dependency Inversion, il Controller
 * comunica solo tramite interfacce. Ogni "Smell Code" legato ai cast espliciti
 * è stato rimosso per garantire la massima stabilità e pulizia del codice.
 */
public class Controller {

    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    // Riferimenti ai DAO definiti esclusivamente tramite interfacce
    private final HackathonDAO hackathonDAO;
    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final DocumentDAO documentDAO;
    private final VoteDAO voteDAO;
    private final FeedbackDAO feedbackDAO;

    private User loggedInUser;
    private boolean recentlyKicked = false;

    /**
     * Inizializza il Controller iniettando le implementazioni concrete dei DAO.
     */
    public Controller() {
        this.userDAO = new UserDAOImpl();
        this.hackathonDAO = new HackathonDAOImpl();
        this.teamDAO = new TeamDAOImpl();
        this.documentDAO = new DocumentDAOImpl();
        this.voteDAO = new VoteDAOImpl();
        this.feedbackDAO = new FeedbackDAOImpl();
    }

    // --- GESTIONE STATO UTENTE ---

    public boolean wasRecentlyKicked() { return recentlyKicked; }
    public void resetKickedFlag() { this.recentlyKicked = false; }
    public User getCurrentUser() { return this.loggedInUser; }

    /**
     * Esegue il login e risolve dinamicamente il ruolo dell'utente.
     */
    public void loginUser(String name, String password) throws BlankFieldException, UserNotFoundException {
        if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BlankFieldException("Username e Password sono obbligatori!");
        }

        User baseUser = userDAO.checkLogin(name, password);
        if (baseUser == null) {
            throw new UserNotFoundException("Credenziali errate o utente inesistente.");
        }

        this.loggedInUser = resolveActualUserRole(baseUser);
        LOGGER.log(Level.INFO, "Login autorizzato per: {0} (Ruolo: {1})",
                new Object[]{name, this.loggedInUser.getClass().getSimpleName()});
    }

    /**
     * Risoluzione polimorfica del ruolo senza l'uso di cast espliciti.
     */
    private User resolveActualUserRole(User u) {
        int uid = u.getUserId();

        // Controllo appartenenza a un Team
        int tId = teamDAO.getTeamIdByUserId(uid);
        if (tId > 0) return new Participant(uid, u.getName(), u.getEmail(), u.getPassword(), tId);

        // Controllo ruolo Giudice
        int judgeHid = userDAO.getHackathonIdWhereUserIsJudge(uid);
        if (judgeHid > 0) return new Judge(uid, u.getName(), u.getEmail(), u.getPassword(), judgeHid);

        // Controllo ruolo Organizzatore
        int orgHid = hackathonDAO.getHackathonIdWhereUserIsOrganizer(uid);
        if (orgHid > 0) return new Organizer(uid, u.getName(), u.getEmail(), u.getPassword(), orgHid);

        return u;
    }

    public void registerUser(String username, String email, String password)
            throws BlankFieldException, UsernameAlreadyTakenException, EmailAlreadyTakenException {
        if (username == null || email == null || password == null) throw new BlankFieldException("Campi obbligatori.");

        if (userDAO.isUsernameAlreadyRegistered(username)) throw new UsernameAlreadyTakenException("Username occupato.");
        if (userDAO.isEmailAlreadyRegistered(email)) throw new EmailAlreadyTakenException("Email occupata.");

        userDAO.registerUser(new User(0, username, email, password));
    }

    // --- LOGICA HACKATHON ---

    public List<Hackathon> getAllHackathons() {
        return hackathonDAO.getAllHackathons();
    }

    public void joinHackathon(int hackathonId) throws Exception {
        if (!loggedInUser.getClass().equals(User.class)) throw new Exception("Hai già un ruolo attivo.");

        int existingReg = userDAO.getRegisteredHackathonId(loggedInUser.getUserId());
        if (existingReg > 0) throw new Exception("Sei già iscritto a un evento.");

        userDAO.registerUserToHackathon(loggedInUser.getUserId(), hackathonId);
    }

    public Hackathon getCurrentHackathon() {
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

    private int resolveCurrentHackathonId() {
        if (loggedInUser instanceof Organizer) return ((Organizer) loggedInUser).getHackathonId();
        if (loggedInUser instanceof Judge) return ((Judge) loggedInUser).getHackathonId();
        if (loggedInUser instanceof Participant) return teamDAO.getHackathonIdByTeam(((Participant) loggedInUser).getTeamId());
        return userDAO.getRegisteredHackathonId(loggedInUser.getUserId());
    }

    public void createHackathon(String title, String location, LocalDate startDate, LocalDate endDate, int maxP, int maxT) throws Exception {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59);

        Hackathon newEvent = new Hackathon.Builder()
                .title(title).location(location)
                .startDate(start).endDate(end)
                .registrationStartDate(start.minusDays(30))
                .registrationEndDate(start.minusDays(1))
                .maxParticipants(maxP).maxTeamSize(maxT).build();

        hackathonDAO.createHackathon(newEvent);
        userDAO.promoteToOrganizer(loggedInUser.getUserId(), newEvent.getHackathonId());
        this.loggedInUser = resolveActualUserRole(loggedInUser);
    }

    // --- LOGICA TEAM E DOCUMENTI ---

    public String createTeamAction(String teamName) throws Exception {
        int hId = userDAO.getRegisteredHackathonId(loggedInUser.getUserId());
        if (hId <= 0) throw new IllegalStateException("Iscriviti prima a un evento.");

        Team newTeam = new Team(0, teamName, "", null, hId);
        int teamId = teamDAO.createTeamAndReturnId(newTeam);

        if (teamId <= 0) throw new Exception("Errore creazione team.");

        teamDAO.linkUserToTeam(loggedInUser.getUserId(), teamId);
        userDAO.removeFromLimbo(loggedInUser.getUserId(), hId);
        this.loggedInUser = resolveActualUserRole(loggedInUser);

        return newTeam.getAccessCode();
    }

    public void joinTeamAction(String accessCode) throws Exception {
        int teamId = teamDAO.getTeamIdByCode(accessCode);
        if (teamId <= 0) throw new Exception("Codice errato.");

        teamDAO.linkUserToTeam(loggedInUser.getUserId(), teamId);
        userDAO.removeFromLimbo(loggedInUser.getUserId(), userDAO.getRegisteredHackathonId(loggedInUser.getUserId()));
        this.loggedInUser = resolveActualUserRole(loggedInUser);
    }

    public void addDocumentAction(String name, String url) throws Exception {
        if (!(loggedInUser instanceof Participant)) throw new IllegalStateException("Solo i partecipanti caricano file.");
        Participant p = (Participant) loggedInUser;
        documentDAO.uploadDocument(new Document(0, name, url, LocalDateTime.now(), p.getTeamId(), resolveCurrentHackathonId()));
    }

    // --- LOGICA GIUDICI E VOTI ---

    public List<User> getUsersInLimbo() {
        Hackathon h = getCurrentHackathon();
        if (h == null) return new ArrayList<>();
        return userDAO.getUsersInLimboByHackathon(h.getHackathonId());
    }

    public void promoteToJudgeAction(int userId) throws Exception {
        userDAO.promoteToJudge(userId, getCurrentHackathon().getHackathonId());
    }

    public boolean voteTeamAction(int teamId, int score) {
        return voteDAO.insertVote(loggedInUser.getUserId(), teamId, score);
    }

    public void saveFeedbackAction(int documentId, String text) throws Exception {
        feedbackDAO.saveOrUpdateFeedback(loggedInUser.getUserId(), documentId, text);
    }

    // --- CLASSIFICHE ---

    public List<String> getFinalRanking() throws Exception {
        Hackathon h = getCurrentHackathon();
        if (h == null || LocalDateTime.now().isBefore(h.getEndDate())) {
            throw new IllegalStateException("Classifica disponibile solo al termine dell'evento.");
        }
        return voteDAO.getLeaderboard(h.getHackathonId());
    }

    public List<String> getLiveRankingForOrganizer() throws Exception {
        return voteDAO.getLeaderboard(getCurrentHackathon().getHackathonId());
    }

    // Metodi Utility per la GUI
    public String getOrganizerNameForHackathon(int id) { return hackathonDAO.getOrganizerUsernameByHackathonId(id); }
    public boolean isCurrentUserOrganizer() { return loggedInUser instanceof Organizer; }
    public boolean isCurrentUserJudge() { return loggedInUser instanceof Judge; }
    public List<Participant> getMyTeamMembers() { return teamDAO.getTeamMembers(((Participant)loggedInUser).getTeamId()); }
    public List<Document> getMyTeamDocuments() { return documentDAO.getDocumentsByTeam(((Participant)loggedInUser).getTeamId()); }
    /**
     * Verifica se l'utente corrente ha i permessi per creare un nuovo Hackathon.
     * Un utente può creare un evento solo se non riveste già un ruolo attivo
     * (Organizer, Judge o Participant) in un altro hackathon.
     * * @return true se l'utente è un utente base senza ruoli speciali.
     */
    public boolean canUserCreateHackathon() {
        if (loggedInUser == null) return false;

        // Se l'utente è un'istanza pura di User (e non una sottoclasse), può creare eventi
        return loggedInUser.getClass().equals(User.class);
    }

    // --- METODI DI UTILITY PER LA DASHBOARD GIUDICE (RISOLUZIONE ERRORI) ---

    /**
     * Recupera tutti i team iscritti all'hackathon a cui è assegnato il giudice corrente.
     * @return Lista di Team dell'hackathon attuale.
     */
    public List<Team> getTeamsByHackathon() {
        int hId = resolveCurrentHackathonId();
        if (hId <= 0) return new ArrayList<>();
        return teamDAO.getTeamsByHackathon(hId);
    }

    /**
     * Recupera i membri di un team specifico.
     * @param teamId ID del team da ispezionare.
     * @return Lista di partecipanti del team.
     */
    public List<Participant> getTeamMembers(int teamId) {
        return teamDAO.getTeamMembers(teamId);
    }

    /**
     * Recupera i documenti caricati da un team specifico.
     * @param teamId ID del team.
     * @return Lista di documenti.
     */
    public List<Document> getTeamDocuments(int teamId) {
        return documentDAO.getDocumentsByTeam(teamId);
    }

    /**
     * Recupera il feedback precedentemente salvato dal giudice corrente per un documento.
     * @param documentId ID del documento.
     * @return Il testo del feedback o stringa vuota se non presente.
     */
    public String getMyFeedbackForDocument(int documentId) {
        if (loggedInUser == null) return "";
        return feedbackDAO.getFeedbackText(loggedInUser.getUserId(), documentId);
    }

    /**
     * Verifica se il giudice corrente ha già espresso un voto per un determinato team.
     * @param teamId ID del team.
     * @return true se il voto è già presente.
     */
    public boolean hasJudgeAlreadyVoted(int teamId) {
        if (loggedInUser == null) return false;
        return voteDAO.checkIfAlreadyVoted(loggedInUser.getUserId(), teamId);
    }

    // --- METODI DI UTILITY PER TEAM E FEEDBACK (RISOLUZIONE ERRORI TEAMCARDPANEL) ---

    /**
     * Recupera l'oggetto Team associato al partecipante attualmente loggato.
     * @return L'entità Team o null se l'utente non è un partecipante.
     */
    public Team getMyTeam() {
        if (loggedInUser instanceof Participant) {
            int teamId = ((Participant) loggedInUser).getTeamId();
            return teamDAO.getTeamById(teamId);
        }
        return null;
    }

    /**
     * Recupera la lista completa di feedback (commenti dei giudici) per un documento.
     * @param documentId ID del documento di cui visualizzare lo storico.
     * @return Lista di oggetti Feedback.
     */
    public List<Feedback> getDocumentFeedbacks(int documentId) {
        return feedbackDAO.getAllFeedbacksForDocument(documentId);
    }
    /**
     * Aggiorna la descrizione del problema (Problem Statement) dell'hackathon corrente.
     * Metodo utilizzato dall'organizzatore tramite la HackathonCardPanel.
     *
     * @param description Il nuovo testo della traccia.
     * @return true se l'operazione è stata completata, false altrimenti.
     */
    public boolean updateHackathonProblem(String description) {
        Hackathon current = getCurrentHackathon();
        if (current == null) {
            LOGGER.log(Level.WARNING, "Tentativo di aggiornamento problema senza un hackathon attivo.");
            return false;
        }

        try {
            // Chiamata all'interfaccia DAO per l'aggiornamento su DB
            hackathonDAO.updateProblemDescription(current.getHackathonId(), description);
            LOGGER.log(Level.INFO, "Problem Statement aggiornato con successo per Hackathon ID: {0}",
                    current.getHackathonId());
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiornamento del problema nel Controller", e);
            return false;
        }
    }


}