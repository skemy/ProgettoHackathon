package dao;

import model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaccia DAO per la gestione della persistenza degli Utenti.
 * Definisce i contratti per l'autenticazione, la registrazione e la risoluzione dei ruoli.
 * <p>
 * Nota Architetturale: Isola il layer Control dai dettagli di PostgreSQL, permettendo
 * una gestione centralizzata della sicurezza e dei ruoli utente (RBAC).
 */
public interface UserDAO {

	/**
	 * Registra un nuovo utente nel sistema.
	 * @param user L'oggetto User contenente i dati anagrafici.
	 * @throws SQLException In caso di errore di persistenza.
	 */
	void registerUser(User user) throws SQLException;

	/**
	 * Esegue l'autenticazione e la risoluzione dinamica del ruolo utente.
	 * @param loginInput Email o Username.
	 * @param password Password in chiaro.
	 * @return L'istanza specifica dell'utente (Organizer, Judge, Participant) o null.
	 * @throws SQLException In caso di errore di lettura dal database.
	 */
	User checkLogin(String loginInput, String password) throws SQLException;

	/**
	 * Verifica se un indirizzo email è già registrato nel sistema.
	 * @param email L'email da controllare.
	 * @return true se l'email esiste già.
	 * @throws SQLException In caso di errore SQL.
	 */
	boolean isEmailAlreadyRegistered(String email) throws SQLException;

	/**
	 * Verifica se un username è già in uso.
	 * @param username L'username da controllare.
	 * @return true se l'username esiste già.
	 * @throws SQLException In caso di errore SQL.
	 */
	boolean isUsernameAlreadyRegistered(String username) throws SQLException;

	/**
	 * Iscrive temporaneamente un utente a un hackathon (stato di Limbo).
	 * @param userId ID utente.
	 * @param hackathonId ID hackathon.
	 * @throws SQLException In caso di errore di inserimento.
	 */
	void registerUserToHackathon(int userId, int hackathonId) throws SQLException;

	/**
	 * Recupera la lista di tutti gli utenti iscritti a un evento ma non assegnati a team o ruoli.
	 * @param hackathonId L'identificativo dell'evento.
	 * @return Una lista di oggetti User.
	 * @throws SQLException In caso di errore di lettura.
	 */
	List<User> getUsersInLimboByHackathon(int hackathonId) throws SQLException;

	/**
	 * Promuove un utente al ruolo di Giudice per lo specifico evento.
	 * @param userId ID utente.
	 * @param hackathonId ID evento.
	 * @return true se la promozione ha avuto successo.
	 * @throws SQLException In caso di errore durante l'operazione.
	 */
	boolean promoteToJudge(int userId, int hackathonId) throws SQLException;

	/**
	 * Rimuove un utente dalla lista delle iscrizioni temporanee (Limbo).
	 * @param userId ID utente.
	 * @param hackathonId ID evento.
	 * @throws SQLException In caso di errore SQL.
	 */
	void removeFromLimbo(int userId, int hackathonId) throws SQLException;

	/**
	 * Recupera un utente generico tramite il suo ID.
	 * @param userId ID utente.
	 * @return L'oggetto User o null.
	 * @throws SQLException In caso di errore SQL.
	 */
	User getUserById(int userId) throws SQLException;

	/**
	 * Recupera l'ID dell'hackathon a cui l'utente è iscritto nel Limbo.
	 * @param userId ID utente.
	 * @return L'ID dell'hackathon o -1.
	 * @throws SQLException In caso di errore SQL.
	 */
	int getRegisteredHackathonId(int userId) throws SQLException;

	/**
	 * Promuove l'utente al ruolo di Organizzatore per un evento creato.
	 * @param userId ID utente.
	 * @param hackathonId ID evento.
	 * @throws SQLException In caso di errore SQL.
	 */
	void promoteToOrganizer(int userId, int hackathonId) throws SQLException;

	/**
	 * Restituisce l'ID dell'hackathon se l'utente ricopre il ruolo di Giudice.
	 * @param userId ID utente.
	 * @return ID hackathon o -1.
	 * @throws SQLException In caso di errore SQL.
	 */
	int getHackathonIdWhereUserIsJudge(int userId) throws SQLException;

	/**
	 * Esegue la pulizia automatica degli utenti rimasti nel Limbo all'avvio dell'evento.
	 * @param hackathonId ID hackathon.
	 * @throws SQLException In caso di errore SQL.
	 */
	void cleanupLimboRegistrations(int hackathonId) throws SQLException;

}