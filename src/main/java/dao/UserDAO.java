package dao;

import model.*;
import java.util.List;

public interface UserDAO {
	// ... metodi esistenti ...

	/**
	 * Rimuove le registrazioni degli utenti che non hanno trovato un team
	 * prima dell'inizio dell'hackathon.
	 * @param hackathonId ID dell'evento.
	 */
	void cleanupLimboRegistrations(int hackathonId);

	/**
	 * Recupera l'ID dell'hackathon per il quale l'utente è registrato come giudice.
	 * @param userId ID dell'utente.
	 * @return ID dell'hackathon o -1 se non è un giudice.
	 */
	int getHackathonIdWhereUserIsJudge(int userId);


	void registerUser(User user);
	User checkLogin(String loginInput, String password);
	boolean isEmailAlreadyRegistered(String email);
	boolean isUsernameAlreadyRegistered(String username);
	void registerUserToHackathon(int userId, int hackathonId);
	List<User> getUsersInLimboByHackathon(int hackathonId);
	boolean promoteToJudge(int userId, int hackathonId);
	void removeFromLimbo(int userId, int hackathonId);
	User getUserById(int userId);
	int getRegisteredHackathonId(int userId);
	void promoteToOrganizer(int userId, int hackathonId);
}