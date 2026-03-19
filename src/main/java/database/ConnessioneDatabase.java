package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe singleton per la gestione della connessione al database PostgreSQL.
 * Fornisce un'istanza unica di connessione per evitare connessioni multiple e gestire la riconnessione se necessario.
 */
public class ConnessioneDatabase {

	private static ConnessioneDatabase instance;
	public Connection connection = null;

	private String nome = "postgres";
	private String password = "060705";

	private String url = "jdbc:postgresql://localhost:5432/HackathonDB";
	private String driver = "org.postgresql.Driver";

	private ConnessioneDatabase() throws SQLException {
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, nome, password);
			System.out.println("✅ Connessione riuscita a: " + url);
		} catch (ClassNotFoundException ex) {
			System.out.println("❌ Driver non trovato: " + ex.getMessage());
			ex.printStackTrace();
		} catch (SQLException ex) {
			System.out.println("❌ Errore Connessione SQL: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Restituisce l'istanza unica della classe ConnessioneDatabase.
	 * Se l'istanza non esiste o la connessione è chiusa, ne crea una nuova.
	 * @return L'istanza di ConnessioneDatabase.
	 * @throws SQLException Se si verifica un errore durante la creazione della connessione.
	 */
	public static ConnessioneDatabase getInstance() throws SQLException {
		if (instance == null) {
			instance = new ConnessioneDatabase();
		} else if (instance.connection.isClosed()) {
			instance = new ConnessioneDatabase();
		}
		return instance;
	}

	/**
	 * Restituisce l'oggetto Connection per eseguire query sul database.
	 * @return L'oggetto Connection attivo.
	 */
	public Connection getConnection() {
		return connection;
	}
}