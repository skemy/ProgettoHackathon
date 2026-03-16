package dao;

import model.User;
import java.util.List;

/**
 * Interfaccia che definisce le operazioni di persistenza per l'entità User.
 * Fornisce i metodi per la gestione dell'autenticazione, della registrazione e del recupero dati.
 */
public interface UserDAO {

	/**
	 * Registra un nuovo utente nel sistema.
	 * @param user L'oggetto User contenente i dati da salvare.
	 */
	public abstract void registerUser(User user);

	/**
	 * Verifica le credenziali di accesso di un utente.
	 * @param email L'indirizzo email (o username, a seconda della logica) fornito per il login.
	 * @param password La password fornita per il login.
	 * @return L'oggetto User se le credenziali sono corrette, null altrimenti.
	 */
	public abstract User checkLogin(String email, String password);

	/**
	 * Recupera un utente specifico tramite il suo identificativo univoco.
	 * @param id L'ID dell'utente da ricercare.
	 * @return L'oggetto User trovato o null se non esiste corrispondenza.
	 */
	public abstract User getUserById(int id);

	/**
	 * Restituisce la lista completa di tutti gli utenti registrati.
	 * @return Una List di oggetti User.
	 */
	public abstract List<User> getAllUsers();

	/**
	 * Verifica se un username è già presente nel database.
	 * @param username L'username da controllare.
	 * @return true se esiste già (allarme!), false altrimenti (via libera).
	 */
	public abstract boolean isUsernameAlreadyRegistered(String username);

	/**
	 * Verifica se un'email è già presente nel database.
	 * @param email L'email da controllare.
	 * @return true se esiste già (allarme!), false altrimenti (via libera).
	 */
	public abstract boolean isEmailAlreadyRegistered(String email);

	public abstract void promoteToOrganizer(int userId, int hackathonId);

	public int getRegisteredHackathonId(int userId);

	public abstract List<User> getUsersInLimboByHackathon(int hackathonId);

	public abstract boolean promoteToJudge(int userId, int hackathonId);

	public int getHackathonIdWhereUserIsJudge(int userId);
}