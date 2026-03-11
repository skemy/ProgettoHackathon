package controller;

import dao.HackathonDAO;
import dao.HackathonDAOImpl;
import database.ConnessioneDatabase;
import exceptions.BlankFieldException;
import exceptions.EmailAlreadyTakenException;
import exceptions.UserNotFoundException;
import exceptions.UsernameAlreadyTakenException;

import dao.UserDAO;
import dao.UserDAOImpl;
import model.Hackathon;
import model.Organizer;
import model.Team;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Il "Cervello" dell'applicazione.
 * Riceve gli input dalla GUI (Boundary) e orchestra la logica chiamando i DAO.
 */
public class Controller {

    // --- SALA MACCHINE (I DAO e la Sessione) ---
    private HackathonDAO hackathonDAO;
    private UserDAO userDAO;
    private User loggedInUser; // Memoria: ricorda chi è attualmente connesso all'app

    public Controller() {
        // Accendiamo il motore verso PostgreSQL!
        this.userDAO = new UserDAOImpl();
        this.hackathonDAO = new HackathonDAOImpl();
    }

    /**
     * Restituisce l'utente attualmente loggato nel sistema.
     * @return User l'utente loggato, o null se nessuno ha effettuato l'accesso.
     */
    public User getCurrentUser() {
        return this.loggedInUser;
    }

    /**
     * Tenta l'autenticazione dell'utente.
     * Chiamato dal LoginCardPanel.
     */
    public void loginUser(String name, String password) throws BlankFieldException, UserNotFoundException {

        // 1. Validazione Input (Il "Cosa")
        if (name == null || name.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new BlankFieldException("Attenzione: Username e Password non possono essere vuoti!");
        }

        System.out.println("🔄 Controller sta interrogando il Database per le credenziali di: " + name);

        // --- LOGICA REALE VERSO IL DB ---
        // CORREZIONE 1: Usiamo checkLogin al posto di authenticateUser
        User user = userDAO.checkLogin(name, password);

        if (user == null) {
            throw new UserNotFoundException("Utente non trovato o password errata.");
        }

        // 2. Salviamo la "Sessione" dell'utente!
        this.loggedInUser = user;

        // CORREZIONE 2: Usiamo getName() al posto di getUsername()
        System.out.println("✅ Login confermato dal Database! Benvenuto a bordo, " + user.getName());
        // 2. Salviamo la "Sessione" dell'utente!
        this.loggedInUser = user;

        // AGGIUNGI QUESTA RIGA:
        System.out.println("🚨 DEBUG LOGIN: La classe dell'utente in memoria è: " + this.loggedInUser.getClass().getSimpleName());
    }


    /**
     * Tenta la registrazione di un nuovo utente.
     * Chiamato dal RegistrationCardPanel.
     */
    public void registerUser(String username, String email, String password)
            throws BlankFieldException, UsernameAlreadyTakenException, EmailAlreadyTakenException {

        // 1. Validazione Input (Il "Cosa")
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new BlankFieldException("Attenzione: Tutti i campi sono obbligatori!");
        }

        System.out.println("🔄 Controller sta verificando le collisioni nel Database...");

        // --- LOGICA REALE VERSO IL DB ---
        // 2. Controllo Esistenza (Le tue "Guardie")
        if (userDAO.isUsernameAlreadyRegistered(username)) {
            throw new UsernameAlreadyTakenException("Questo Username è già stato scelto da un altro utente.");
        }

        if (userDAO.isEmailAlreadyRegistered(email)) {
            throw new EmailAlreadyTakenException("Esiste già un account con questa Email.");
        }

        // 3. Salvataggio definitivo
        userDAO.registerUser(new User(username, email, password));

        System.out.println("✅ Utente salvato permanentemente nel Database PostgreSQL!");
    }

    // =========================================================================
    // --- METODI PER LA GESTIONE HACKATHON ---
    // =========================================================================

    /**
     * Recupera la lista di tutti gli hackathon dal database.
     */
    public List<Hackathon> getAllHackathons() {
        System.out.println("🔄 Controller: Recupero lista eventi dal database...");
        // Chiamata al metodo che hai già scritto nel DAO
        return hackathonDAO.getAllHackathons();
    }

    /**
     * Crea un nuovo Hackathon e promuove l'utente attuale a Organizer.
     */

    public void createHackathon(String title, String location, LocalDate startDate, LocalDate endDate, int maxParticipants, int maxTeamSize) throws Exception {

        // 1. Validazione Input di base
        if (title == null || title.trim().isEmpty() || location == null || location.trim().isEmpty()) {
            throw new BlankFieldException("Titolo e Location sono obbligatori.");
        }

        // 2. Controllo Ruolo: Solo gli utenti "base" possono creare un nuovo Hackathon.
        // Se sei già Organizer, Judge o Participant, la classe non sarà esattamente "User.class".
        if (!loggedInUser.getClass().equals(User.class)) {
            throw new Exception("Non puoi creare un nuovo evento: fai già parte di un Hackathon attivo.");
        }

        System.out.println("🔄 Creazione dell'evento nel Database...");

        // Conversione date (da LocalDate della GUI a LocalDateTime del DB)
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59);

        // Date di registrazione (per ora le impostiamo in automatico, es. da 30gg prima a 1gg prima)
        LocalDateTime regStart = start.minusDays(30);
        LocalDateTime regEnd = start.minusDays(1);

        // 3. Creiamo l'Entity con ID 0 (fittizio)
        Hackathon newEvent = new Hackathon(0, title, location, start, end, regStart, regEnd, maxParticipants, maxTeamSize, null);

        // 4. Deleghiamo al DAO. Il DAO salva e INIETTA il vero ID dentro "newEvent"
        hackathonDAO.createHackathon(newEvent);

        // 5. Ora newEvent ha l'ID reale!
        int realHackathonId = newEvent.getHackathonId();

        if (realHackathonId > 0) {
            // Promuoviamo l'utente nella tabella "organizer" del Database
            // (Assicurati di aver aggiunto questo metodo in UserDAOImpl come discusso prima)
            if (userDAO instanceof UserDAOImpl) {
                ((UserDAOImpl) userDAO).promoteToOrganizer(loggedInUser.getUserId(), realHackathonId);
            }

            // Aggiorniamo la Sessione in memoria per sbloccare i permessi GUI
            this.loggedInUser = new Organizer(
                    loggedInUser.getUserId(),
                    loggedInUser.getName(),
                    loggedInUser.getEmail(),
                    loggedInUser.getPassword(),
                    realHackathonId
            );

            System.out.println("🎉 Evento creato con successo! Ora sei l'Organizzatore dell'evento ID " + realHackathonId);
        } else {
            throw new Exception("Errore critico durante la generazione dell'ID dell'evento.");
        }
    }


    /**
     * Promuove un utente base a Organizzatore collegandolo a un Hackathon.
     */
    public void promoteToOrganizer(int userId, int hackathonId) {
        String query = "INSERT INTO organizer (userId, hackathonId) VALUES (?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setInt(2, hackathonId);
            ps.executeUpdate();
            System.out.println("✅ Utente promosso a Organizer nel DB.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Iscrive l'utente base a un hackathon.
     */
    /**
     * Iscrive un utente a un hackathon, facendolo diventare Partecipante.
     * @param hackathonId L'ID dell'evento a cui vuole iscriversi
     */
    /**
     * Iscrive un utente a un hackathon, facendolo diventare Partecipante.
     * @param hackathonId L'ID dell'evento a cui vuole iscriversi
     */
    public void joinHackathon(int hackathonId) throws Exception {

        if (!loggedInUser.getClass().equals(User.class)) {
            throw new Exception("Azione bloccata: Fai già parte di un Hackathon attivo.");
        }

        System.out.println("🔄 L'utente è libero. Procedo con l'iscrizione all'Hackathon ID: " + hackathonId);

        /* * [STUB DEL DATABASE]
         * Qui chiameremo il TeamDAO per inserire l'utente nel DB nelle tabelle 'team' e 'participation'.
         */
        System.out.println("🛠️ [STUB DB] L'utente è stato inserito nel Database come Participant!");

        // --- LA CORREZIONE DELLA SESSIONE ---
        // Promuoviamo l'utente in memoria. Così al prossimo click, la guardia scatterà!
        int fakeTeamId = 999; // Finto per ora, in attesa del TeamDAO
        this.loggedInUser = new model.Participant(
                loggedInUser.getUserId(),
                loggedInUser.getName(),
                loggedInUser.getEmail(),
                loggedInUser.getPassword(),
                fakeTeamId
        );
        System.out.println("✅ Memoria aggiornata: L'utente ora è un Participant!");
    }

    // ==========================================================
    // METODI STUB PER LA GUI HACKATHON CARD PANEL (BCE PATTERN)
    // ==========================================================

// ==========================================================
    // METODI PER LA GUI HACKATHON CARD PANEL (BCE PATTERN)
    // ==========================================================
// ==========================================================
    // METODI PER LA GUI HACKATHON CARD PANEL (BCE PATTERN)
    // ==========================================================

    /**
     * Recupera l'Hackathon in base al ruolo dell'utente attuale.
     */
    public Hackathon getCurrentHackathon() {
        if (loggedInUser == null) return null;

        System.out.println("-------------------------------------------------");
        System.out.println("🕵️ INDAGINE HACKATHON PER: " + loggedInUser.getName() + " (Classe: " + loggedInUser.getClass().getSimpleName() + ")");

        int hId = -1;

        // 1. Capiamo chi è e che ID deve cercare
        if (loggedInUser instanceof Organizer) {
            hId = ((Organizer) loggedInUser).getHackathonId();
            System.out.println("🔎 Ruolo: Organizer. Cerco Hackathon ID: " + hId);
        } else if (loggedInUser instanceof model.Judge) {
            hId = ((model.Judge) loggedInUser).getHackathonId();
            System.out.println("🔎 Ruolo: Judge. Cerco Hackathon ID: " + hId);
        } else if (loggedInUser instanceof model.Participant) {
            int teamId = ((model.Participant) loggedInUser).getTeamId();
            hId = getHackathonIdFromTeam(teamId);
            System.out.println("🔎 Ruolo: Participant (Team ID: " + teamId + "). Hackathon ID trovato: " + hId);
        } else {
            System.out.println("🚫 Ruolo: Semplice User. Nessun hackathon associato. (È CORRETTO CHE VEDA N/A E IL POPUP)");
            return null;
        }

        // 2. Controllo di sicurezza sull'ID
        if (hId <= 0) {
            System.out.println("❌ ERRORE: L'ID dell'hackathon è " + hId + ". Impossibile cercare nel DB.");
            return null;
        }

        // 3. Estrazione dal DB con trappola per errori silenziosi
        try {
            Hackathon h = hackathonDAO.getHackathonById(hId);
            if (h == null) {
                System.out.println("❌ ERRORE CRITICO: Il DAO ha restituito NULL per l'ID " + hId + "!");
            } else {
                System.out.println("✅ SUCCESSO: Hackathon trovato! Titolo: " + h.getTitle());
            }
            System.out.println("-------------------------------------------------");
            return h;
        } catch (Exception e) {
            System.out.println("🔥 ECCEZIONE ESPLOSIVA durante l'estrazione dal DB:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper temporaneo: trova l'hackathonId a cui appartiene un Team.
     */
    private int getHackathonIdFromTeam(int teamId) {
        String query = "SELECT hackathonId FROM team WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("hackathonId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Verifica se l'utente attualmente loggato ha il ruolo di Giudice.
     */
    public boolean isCurrentUserJudge() {
        return loggedInUser instanceof model.Judge;
    }

    /**
     * Recupera il nome dell'organizzatore partendo dall'ID dell'hackathon.
     */
    public String getOrganizerNameForHackathon(int hackathonId) {
        if (hackathonDAO instanceof HackathonDAOImpl) {
            return ((HackathonDAOImpl) hackathonDAO).getOrganizerUsernameByHackathonId(hackathonId);
        }
        return "Unknown";
    }

    public boolean updateHackathonProblem(String newDescription) {
        Hackathon currentHackathon = getCurrentHackathon();
        if (currentHackathon != null) {
            hackathonDAO.updateProblemDescription(currentHackathon.getHackathonId(), newDescription);
            return true;
        }
        return false;
    }

    public List<Team> getRankedTeams() {
        return new ArrayList<>();
    }


}