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
 * Orchestratore centrale del sistema (Layer Control nel pattern BCE).
 * Coordina il flusso di dati tra la Boundary (GUI) e l'Entity Access (DAO).
 * <p>
 * Nota Architetturale: Gestisce la propagazione delle eccezioni SQL e implementa
 * i controlli di integrità temporale per impedire modifiche post-evento.
 */
public class Controller {

    private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

    private final HackathonDAO hackathonDAO;
    private final UserDAO userDAO;
    private final TeamDAO teamDAO;
    private final DocumentDAO documentDAO;
    private final VoteDAO voteDAO;
    private final FeedbackDAO feedbackDAO;

    private User loggedInUser;
    private boolean recentlyKicked = false;

    /**
     * Inizializza il controller istanziando le implementazioni dei DAO.
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
     * Verifica se l'hackathon associato all'utente corrente è ancora attivo.
     * Impedisce qualsiasi operazione di scrittura dopo la data di fine.
     * @throws IllegalStateException Se l'evento è terminato o non esistente.
     * @throws SQLException In caso di errore DB.
     */
    private void ensureHackathonIsActive() throws SQLException {
        Hackathon current = getCurrentHackathon();
        if (current == null) {
            throw new IllegalStateException("Nessun hackathon attivo trovato per questa operazione.");
        }

        // Confronto tra ora attuale e endDate dell'evento
        if (LocalDateTime.now().isAfter(current.getEndDate())) {
            throw new IllegalStateException("L'evento è terminato il " +
                    current.getEndDate().toLocalDate() + ". Il sistema è ora in modalità sola lettura.");
        }
    }

    // --- GESTIONE STATO UTENTE ---

    public boolean wasRecentlyKicked() { return recentlyKicked; }
    public void resetKickedFlag() { this.recentlyKicked = false; }
    public User getCurrentUser() { return this.loggedInUser; }

    /**
     * Esegue l'autenticazione dell'utente e ne risolve il ruolo.
     */
    public void loginUser(String name, String password) throws BlankFieldException, UserNotFoundException, SQLException {
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

    private User resolveActualUserRole(User u) throws SQLException {
        int uid = u.getUserId();
        int tId = teamDAO.getTeamIdByUserId(uid);
        if (tId > 0) return new Participant(uid, u.getName(), u.getEmail(), u.getPassword(), tId);

        int judgeHid = userDAO.getHackathonIdWhereUserIsJudge(uid);
        if (judgeHid > 0) return new Judge(uid, u.getName(), u.getEmail(), u.getPassword(), judgeHid);

        int orgHid = hackathonDAO.getHackathonIdWhereUserIsOrganizer(uid);
        if (orgHid > 0) return new Organizer(uid, u.getName(), u.getEmail(), u.getPassword(), orgHid);

        return u;
    }

    public void registerUser(String username, String email, String password)
            throws BlankFieldException, UsernameAlreadyTakenException, EmailAlreadyTakenException, SQLException {
        if (username == null || email == null || password == null) throw new BlankFieldException("Mandatory fields missing.");
        if (userDAO.isUsernameAlreadyRegistered(username)) throw new UsernameAlreadyTakenException("Username used.");
        if (userDAO.isEmailAlreadyRegistered(email)) throw new EmailAlreadyTakenException("Email used.");

        userDAO.registerUser(new User(0, username, email, password));
    }

    // --- AZIONI HACKATHON ---

    public List<Hackathon> getAllHackathons() throws SQLException {
        return hackathonDAO.getAllHackathons();
    }

    public void joinHackathon(int hackathonId) throws CannotRegisterToEventException, SQLException {
        // Controllo se l'hackathon a cui ci si vuole iscrivere è già finito
        Hackathon target = hackathonDAO.getHackathonById(hackathonId);
        if (target != null && LocalDateTime.now().isAfter(target.getStartDate())) {
            throw new CannotRegisterToEventException();
        }

        if (!loggedInUser.getClass().equals(User.class)) throw new CannotRegisterToEventException();
        if (userDAO.getRegisteredHackathonId(loggedInUser.getUserId()) > 0) throw new CannotRegisterToEventException();

        userDAO.registerUserToHackathon(loggedInUser.getUserId(), hackathonId);
    }

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

    private int resolveCurrentHackathonId() throws SQLException {
        if (loggedInUser instanceof Organizer org) return org.getHackathonId();
        if (loggedInUser instanceof Judge judge) return judge.getHackathonId();
        if (loggedInUser instanceof Participant part) return teamDAO.getHackathonIdByTeam(part.getTeamId());
        return userDAO.getRegisteredHackathonId(loggedInUser.getUserId());
    }

    public void createHackathon(String title, String location, LocalDate startDate, LocalDate endDate, int maxP, int maxT) throws SQLException {
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

    public boolean updateHackathonProblem(String description) throws SQLException {
        ensureHackathonIsActive(); // PROTEZIONE
        Hackathon current = getCurrentHackathon();
        if (current == null) return false;
        hackathonDAO.updateProblemDescription(current.getHackathonId(), description);
        return true;
    }

    // --- AZIONI TEAM E DOCUMENTI ---

    public String createTeamAction(String teamName) throws SQLException {
        ensureHackathonIsActive(); // PROTEZIONE
        int hId = userDAO.getRegisteredHackathonId(loggedInUser.getUserId());
        if (hId <= 0) throw new IllegalStateException("Iscriviti prima a un evento.");

        Team newTeam = new Team(0, teamName, "", null, hId);
        int teamId = teamDAO.createTeamAndReturnId(newTeam);
        if (teamId <= 0) throw new IllegalArgumentException("Errore creazione team.");

        teamDAO.linkUserToTeam(loggedInUser.getUserId(), teamId);
        userDAO.removeFromLimbo(loggedInUser.getUserId(), hId);
        this.loggedInUser = resolveActualUserRole(loggedInUser);
        return newTeam.getAccessCode();
    }

    public void joinTeamAction(String accessCode) throws SQLException {
        ensureHackathonIsActive(); // PROTEZIONE
        int teamId = teamDAO.getTeamIdByCode(accessCode);
        if (teamId <= 0) throw new IllegalArgumentException("Codice errato.");

        teamDAO.linkUserToTeam(loggedInUser.getUserId(), teamId);
        userDAO.removeFromLimbo(loggedInUser.getUserId(), userDAO.getRegisteredHackathonId(loggedInUser.getUserId()));
        this.loggedInUser = resolveActualUserRole(loggedInUser);
    }

    public void addDocumentAction(String name, String url) throws SQLException {
        ensureHackathonIsActive(); // PROTEZIONE
        if (!(loggedInUser instanceof Participant p)) throw new IllegalStateException("Solo i partecipanti possono caricare progetti.");
        documentDAO.uploadDocument(new Document(0, name, url, LocalDateTime.now(), p.getTeamId(), resolveCurrentHackathonId()));
    }

    // --- AZIONI GIUDICI E VOTI ---

    public void promoteToJudgeAction(int userId) throws SQLException {
        ensureHackathonIsActive(); // PROTEZIONE
        userDAO.promoteToJudge(userId, getCurrentHackathon().getHackathonId());
    }

    public boolean voteTeamAction(int teamId, int score) throws SQLException {
        ensureHackathonIsActive(); // PROTEZIONE
        return voteDAO.insertVote(loggedInUser.getUserId(), teamId, score);
    }

    public void saveFeedbackAction(int documentId, String text) throws SQLException, BlankFieldException {
        ensureHackathonIsActive(); // PROTEZIONE
        feedbackDAO.saveOrUpdateFeedback(loggedInUser.getUserId(), documentId, text);
    }

    // --- METODI DI LETTURA E CLASSIFICHE (NON PROTETTI DA ensureHackathonIsActive) ---

    public List<User> getUsersInLimbo() throws SQLException {
        Hackathon h = getCurrentHackathon();
        return (h == null) ? new ArrayList<>() : userDAO.getUsersInLimboByHackathon(h.getHackathonId());
    }

    public List<String> getFinalRanking() throws SQLException {
        Hackathon h = getCurrentHackathon();
        if (h == null || LocalDateTime.now().isBefore(h.getEndDate())) throw new IllegalStateException("Evento in corso. Classifica non disponibile.");
        return voteDAO.getLeaderboard(h.getHackathonId());
    }

    public List<String> getLiveRankingForOrganizer() throws SQLException {
        return voteDAO.getLeaderboard(getCurrentHackathon().getHackathonId());
    }

    public List<Participant> getMyTeamMembers() throws SQLException {
        return (loggedInUser instanceof Participant p) ? teamDAO.getTeamMembers(p.getTeamId()) : new ArrayList<>();
    }

    public List<Document> getMyTeamDocuments() throws SQLException {
        return (loggedInUser instanceof Participant p) ? documentDAO.getDocumentsByTeam(p.getTeamId()) : new ArrayList<>();
    }

    public Team getMyTeam() throws SQLException {
        return (loggedInUser instanceof Participant p) ? teamDAO.getTeamById(p.getTeamId()) : null;
    }

    public List<Team> getTeamsByHackathon() throws SQLException {
        int hId = resolveCurrentHackathonId();
        return hId > 0 ? teamDAO.getTeamsByHackathon(hId) : new ArrayList<>();
    }

    public List<Participant> getTeamMembers(int teamId) throws SQLException { return teamDAO.getTeamMembers(teamId); }
    public List<Document> getTeamDocuments(int teamId) throws SQLException { return documentDAO.getDocumentsByTeam(teamId); }
    public List<Feedback> getDocumentFeedbacks(int documentId) throws SQLException { return feedbackDAO.getAllFeedbacksForDocument(documentId); }

    public String getMyFeedbackForDocument(int documentId) throws SQLException {
        return loggedInUser != null ? feedbackDAO.getFeedbackText(loggedInUser.getUserId(), documentId) : "";
    }

    public boolean hasJudgeAlreadyVoted(int teamId) throws SQLException {
        return loggedInUser != null && voteDAO.checkIfAlreadyVoted(loggedInUser.getUserId(), teamId);
    }

    public String getOrganizerNameForHackathon(int id) throws SQLException { return hackathonDAO.getOrganizerUsernameByHackathonId(id); }
    public boolean isCurrentUserOrganizer() { return loggedInUser instanceof Organizer; }
    public boolean isCurrentUserJudge() { return loggedInUser instanceof Judge; }
    public boolean canUserCreateHackathon() { return loggedInUser != null && loggedInUser.getClass().equals(User.class); }
}