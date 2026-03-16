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

/**
 * Il "Cervello" dell'applicazione.
 * Riceve gli input dalla GUI (Boundary) e orchestra la logica chiamando i DAO.
 * (100% privo di query SQL dirette).
 */
public class Controller {

    private HackathonDAO hackathonDAO;
    private UserDAO userDAO;
    private User loggedInUser;

    public Controller() {
        this.userDAO = new UserDAOImpl();
        this.hackathonDAO = new HackathonDAOImpl();
    }

    private boolean recentlyKicked = false;
    public boolean wasRecentlyKicked() { return recentlyKicked; }
    public void resetKickedFlag() { this.recentlyKicked = false; }

    public User getCurrentUser() {
        return this.loggedInUser;
    }

    public void loginUser(String name, String password) throws BlankFieldException, UserNotFoundException {
        if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BlankFieldException("Attenzione: Username e Password non possono essere vuoti!");
        }

        User baseUser = userDAO.checkLogin(name, password);
        if (baseUser == null) {
            throw new UserNotFoundException("Utente non trovato o password errata.");
        }

        this.loggedInUser = resolveActualUserRole(baseUser);
        System.out.println("✅ Login confermato! Ruolo reale caricato: " + this.loggedInUser.getClass().getSimpleName());
    }

    private User resolveActualUserRole(User u) {
        int uid = u.getUserId();

        int teamId = new TeamDAOImpl().getTeamIdByUserId(uid);
        if (teamId > 0) return new Participant(uid, u.getName(), u.getEmail(), u.getPassword(), teamId);

        int judgeHid = ((UserDAOImpl) userDAO).getHackathonIdWhereUserIsJudge(uid);
        if (judgeHid > 0) return new Judge(uid, u.getName(), u.getEmail(), u.getPassword(), judgeHid);

        int orgHid = ((HackathonDAOImpl) hackathonDAO).getHackathonIdWhereUserIsOrganizer(uid);
        if (orgHid > 0) return new Organizer(uid, u.getName(), u.getEmail(), u.getPassword(), orgHid);

        return u;
    }

    public void registerUser(String username, String email, String password)
            throws BlankFieldException, UsernameAlreadyTakenException, EmailAlreadyTakenException {

        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new BlankFieldException("Attenzione: Tutti i campi sono obbligatori!");
        }

        if (userDAO.isUsernameAlreadyRegistered(username)) {
            throw new UsernameAlreadyTakenException("Questo Username è già stato scelto.");
        }

        if (userDAO.isEmailAlreadyRegistered(email)) {
            throw new EmailAlreadyTakenException("Esiste già un account con questa Email.");
        }

        userDAO.registerUser(new User(username, email, password));
    }

    public List<Hackathon> getAllHackathons() {
        return hackathonDAO.getAllHackathons();
    }

    public String createTeamAction(String teamName) throws Exception {
        if (!loggedInUser.getClass().equals(User.class)) {
            throw new Exception("Devi essere nel Limbo per creare un team.");
        }

        int hId = ((UserDAOImpl) userDAO).getRegisteredHackathonId(loggedInUser.getUserId());
        if (hId <= 0) throw new Exception("Non sei iscritto a nessun evento.");

        TeamDAOImpl teamDAO = new TeamDAOImpl();
        Team newTeam = new Team(0, teamName, "", null, hId);
        int teamId = teamDAO.createTeamAndReturnId(newTeam);

        if (teamId <= 0) throw new Exception("Errore: Nome del team già in uso o database non raggiungibile.");

        teamDAO.linkUserToTeam(loggedInUser.getUserId(), teamId);
        ((UserDAOImpl) userDAO).removeFromLimbo(loggedInUser.getUserId(), hId);

        this.loggedInUser = new Participant(
                loggedInUser.getUserId(), loggedInUser.getName(),
                loggedInUser.getEmail(), loggedInUser.getPassword(), teamId
        );

        return newTeam.getAccessCode();
    }

    public void joinTeamAction(String accessCode) throws Exception {
        if (!loggedInUser.getClass().equals(User.class)) {
            throw new Exception("Devi essere nel Limbo per unirti a un team.");
        }

        int hId = ((UserDAOImpl) userDAO).getRegisteredHackathonId(loggedInUser.getUserId());
        if (hId <= 0) throw new Exception("Non sei iscritto a nessun evento.");

        TeamDAOImpl teamDAO = new TeamDAOImpl();
        int teamId = teamDAO.getTeamIdByCode(accessCode);
        if (teamId <= 0) throw new Exception("Codice di accesso non valido o team inesistente.");

        boolean joined = teamDAO.linkUserToTeam(loggedInUser.getUserId(), teamId);
        if (!joined) throw new Exception("Errore durante l'adesione al team.");

        ((UserDAOImpl) userDAO).removeFromLimbo(loggedInUser.getUserId(), hId);

        this.loggedInUser = new Participant(
                loggedInUser.getUserId(), loggedInUser.getName(),
                loggedInUser.getEmail(), loggedInUser.getPassword(), teamId
        );
    }

    public boolean canUserCreateHackathon() {
        if (loggedInUser.getClass().equals(User.class)) {
            int existingRegistration = ((dao.UserDAOImpl) userDAO).getRegisteredHackathonId(loggedInUser.getUserId());
            return existingRegistration <= 0;
        }

        Hackathon current = getCurrentHackathon();
        if (current == null) return true;

        return LocalDateTime.now().isAfter(current.getEndDate());
    }

    public void createHackathon(String title, String location, LocalDate startDate, LocalDate endDate, int maxParticipants, int maxTeamSize) throws Exception {
        if (title == null || title.trim().isEmpty() || location == null || location.trim().isEmpty()) {
            throw new BlankFieldException("Titolo e Location sono obbligatori.");
        }

        if (!loggedInUser.getClass().equals(User.class)) {
            throw new Exception("Fai già parte di un Hackathon attivo con un ruolo specifico.");
        }

        int existingRegistration = ((UserDAOImpl) userDAO).getRegisteredHackathonId(loggedInUser.getUserId());
        if (existingRegistration > 0) {
            throw new Exception("Sei già iscritto a un evento! Non puoi crearne uno nuovo finché sei registrato a quello attuale.");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59);
        LocalDateTime regStart = start.minusDays(30);
        LocalDateTime regEnd = start.minusDays(1);

        Hackathon newEvent = new Hackathon(0, title, location, start, end, regStart, regEnd, maxParticipants, maxTeamSize, null);
        hackathonDAO.createHackathon(newEvent);

        int realId = newEvent.getHackathonId();
        if (realId > 0) {
            ((UserDAOImpl) userDAO).promoteToOrganizer(loggedInUser.getUserId(), realId);
            this.loggedInUser = new Organizer(loggedInUser.getUserId(), loggedInUser.getName(), loggedInUser.getEmail(), loggedInUser.getPassword(), realId);
        } else {
            throw new Exception("Errore durante la generazione dell'evento.");
        }
    }

    public void joinHackathon(int hackathonId) throws Exception {
        if (!loggedInUser.getClass().equals(User.class)) {
            throw new Exception("Fai già parte di un Hackathon attivo.");
        }

        int existingRegistration = ((UserDAOImpl) userDAO).getRegisteredHackathonId(loggedInUser.getUserId());
        if (existingRegistration > 0) {
            throw new Exception("Sei già registrato a un evento!");
        }

        ((UserDAOImpl) userDAO).registerUserToHackathon(loggedInUser.getUserId(), hackathonId);
    }

    public Hackathon getCurrentHackathon() {
        if (loggedInUser == null) return null;

        int hId = -1;
        if (loggedInUser instanceof Organizer) {
            hId = ((Organizer) loggedInUser).getHackathonId();
        } else if (loggedInUser instanceof Judge) {
            hId = ((Judge) loggedInUser).getHackathonId();
        } else if (loggedInUser instanceof Participant) {
            hId = new TeamDAOImpl().getHackathonIdByTeam(((Participant) loggedInUser).getTeamId());
        } else {
            hId = ((UserDAOImpl) userDAO).getRegisteredHackathonId(loggedInUser.getUserId());
        }

        if (hId <= 0) return null;

        try {
            Hackathon h = hackathonDAO.getHackathonById(hId);
            if (h != null) {
                if (LocalDateTime.now().isAfter(h.getStartDate()) || LocalDateTime.now().isEqual(h.getStartDate())) {
                    ((UserDAOImpl) userDAO).cleanupLimboRegistrations(hId);
                    if (loggedInUser.getClass().equals(User.class)) {
                        this.recentlyKicked = true;
                        return null;
                    }
                }
            }
            return h;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getOrganizerNameForHackathon(int hackathonId) {
        if (hackathonDAO instanceof HackathonDAOImpl) {
            return ((HackathonDAOImpl) hackathonDAO).getOrganizerUsernameByHackathonId(hackathonId);
        }
        return "Unknown";
    }

    public List<Participant> getMyTeamMembers() {
        if (loggedInUser instanceof Participant) {
            int teamId = ((Participant) loggedInUser).getTeamId();
            return new TeamDAOImpl().getTeamMembers(teamId);
        }
        return new ArrayList<>();
    }

    public boolean isCurrentUserJudge() {
        return loggedInUser instanceof model.Judge;
    }

    public boolean updateHackathonProblem(String newDescription) {
        Hackathon currentHackathon = getCurrentHackathon();
        if (currentHackathon != null) {
            hackathonDAO.updateProblemDescription(currentHackathon.getHackathonId(), newDescription);
            return true;
        }
        return false;
    }

    // --- LOGICA PUBBLICAZIONE CLASSIFICA (LEADERBOARD) ---
    public List<String> getLeaderboardRanking() throws Exception {
        Hackathon current = getCurrentHackathon();
        if (current == null) {
            throw new Exception("No active hackathon found.");
        }

        VoteDAOImpl voteDAO = new VoteDAOImpl();

        // Verifica imposta dalla Traccia: La piattaforma pubblica SE tutti i voti sono acquisiti
        if (!voteDAO.areAllVotesCast(current.getHackathonId())) {
            throw new Exception("Results pending. The platform is waiting for all judges to evaluate all teams.");
        }

        // Se tutti hanno votato, restituisce la classifica formattata
        return voteDAO.getLeaderboard(current.getHackathonId());
    }

    public void addDocumentAction(String name, String url) throws Exception {
        if (!(loggedInUser instanceof Participant)) {
            throw new Exception("Solo i membri ufficiali del team possono caricare documenti.");
        }

        if (name.isBlank() || url.isBlank()) {
            throw new Exception("Nome e URL sono obbligatori.");
        }

        int teamId = ((Participant) loggedInUser).getTeamId();
        Document newDoc = new Document(0, name, url, LocalDateTime.now(), teamId, 0);

        new DocumentDAOImpl().uploadDocument(newDoc);
    }

    public List<Document> getMyTeamDocuments() {
        if (loggedInUser instanceof Participant) {
            return new DocumentDAOImpl().getDocumentsByTeam(((Participant) loggedInUser).getTeamId());
        }
        return new ArrayList<>();
    }

    public Team getMyTeam() {
        if (loggedInUser instanceof Participant) {
            int teamId = ((Participant) loggedInUser).getTeamId();
            return new TeamDAOImpl().getTeamById(teamId);
        }
        return null;
    }

    public List<User> getUsersInLimbo() {
        Hackathon current = getCurrentHackathon();
        if (current == null) return new ArrayList<>();

        List<User> rawList = ((UserDAOImpl) userDAO).getUsersInLimboByHackathon(current.getHackathonId());
        List<User> trueLimbo = new ArrayList<>();

        for (User u : rawList) {
            User resolved = resolveActualUserRole(u);
            if (resolved.getClass().equals(User.class)) {
                trueLimbo.add(u);
            }
        }
        return trueLimbo;
    }

    public void promoteToJudgeAction(int userId) throws Exception {
        Hackathon current = getCurrentHackathon();
        if (current == null) throw new Exception("No active hackathons.");

        int judgeHid = ((UserDAOImpl) userDAO).getHackathonIdWhereUserIsJudge(userId);
        if (judgeHid == current.getHackathonId()) {
            throw new Exception("The user is already a judge");
        }

        boolean success = ((UserDAOImpl) userDAO).promoteToJudge(userId, current.getHackathonId());
        if (!success) {
            throw new Exception("Errore interno del database durante la promozione.");
        }
    }

    public List<Team> getTeamsByHackathon() {
        Hackathon current = getCurrentHackathon();
        if (current == null) return new ArrayList<>();
        return new TeamDAOImpl().getTeamsByHackathon(current.getHackathonId());
    }

    public boolean hasJudgeAlreadyVoted(int teamId) {
        if (loggedInUser instanceof Judge) {
            return new VoteDAOImpl().checkIfAlreadyVoted(loggedInUser.getUserId(), teamId);
        }
        return true;
    }

    public boolean voteTeamAction(int teamId, int score) {
        if (loggedInUser instanceof Judge) {
            return new VoteDAOImpl().insertVote(loggedInUser.getUserId(), teamId, score);
        }
        return false;
    }

    public List<Participant> getTeamMembers(int teamId) {
        return new dao.TeamDAOImpl().getTeamMembers(teamId);
    }

    public List<Document> getTeamDocuments(int teamId) {
        return new dao.DocumentDAOImpl().getDocumentsByTeam(teamId);
    }

    public boolean isCurrentUserOrganizer() {
        return loggedInUser instanceof model.Organizer;
    }

    public String getMyFeedbackForDocument(int documentId) {
        return new FeedbackDAOImpl().getFeedbackText(loggedInUser.getUserId(), documentId);
    }

    public void saveFeedbackAction(int documentId, String text) throws Exception {
        if (!(loggedInUser instanceof Judge)) throw new Exception("Solo i giudici possono commentare.");
        boolean success = new FeedbackDAOImpl().saveOrUpdateFeedback(loggedInUser.getUserId(), documentId, text);
        if (!success) throw new Exception("Errore durante il salvataggio del commento.");
    }

    public List<Feedback> getDocumentFeedbacks(int documentId) {
        return new FeedbackDAOImpl().getAllFeedbacksForDocument(documentId);
    }

    // --- LOGICA PUBBLICAZIONE CLASSIFICA (LEADERBOARD) ---

    /**
     * Usato dall'Organizzatore. Mostra la classifica in tempo reale.
     */
    public List<String> getLiveRankingForOrganizer() throws Exception {
        Hackathon current = getCurrentHackathon();
        if (current == null) throw new Exception("No active hackathon found.");
        return new VoteDAOImpl().getLeaderboard(current.getHackathonId());
    }

    /**
     * Usato da Partecipanti/Giudici. Mostra la classifica SOLO se l'evento è finito.
     */
    public List<String> getFinalRanking() throws Exception {
        Hackathon current = getCurrentHackathon();
        if (current == null) throw new Exception("No active hackathon found.");

        // Se l'evento non è ancora finito, blocca la visualizzazione.
        if (LocalDateTime.now().isBefore(current.getEndDate())) {
            throw new Exception("The event is still ongoing. The final ranking will be published automatically when the event ends.");
        }

        // Se l'evento è finito, mostra la classifica (anche se i giudici hanno dimenticato di votare)
        return new VoteDAOImpl().getLeaderboard(current.getHackathonId());
    }
}