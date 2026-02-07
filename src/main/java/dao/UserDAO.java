package dao;

import model.User;
import java.util.List;

/**
 * Interfaccia che definisce le operazioni di persistenza per l'entità User.
 * Fornisce i metodi per la gestione dell'autenticazione e del recupero dati.
 */
public interface UserDAO {

	/**
	 * Registra un nuovo utente nel sistema.
	 * * @param user L'oggetto User contenente i dati da salvare.
	 */
	public abstract void registerUser(User user);

	/**
	 * Verifica le credenziali di accesso di un utente.
	 * * @param email L'indirizzo email fornito per il login.
	 * @param password La password fornita per il login.
	 * @return L'oggetto User se le credenziali sono corrette, null altrimenti.
	 */
	public abstract User checkLogin(String email, String password);

	/**
	 * Recupera un utente specifico tramite il suo identificativo univoco.
	 * * @param id L'ID dell'utente da ricercare.
	 * @return L'oggetto User trovato o null se non esiste corrispondenza.
	 */
	public abstract User getUserById(int id);

	/**
	 * Restituisce la lista completa di tutti gli utenti registrati.
	 * * @return Una List di oggetti User.
	 */
	public abstract List<User> getAllUsers();
}