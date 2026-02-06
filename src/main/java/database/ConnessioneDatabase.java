package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnessioneDatabase {

	private static ConnessioneDatabase instance;
	public Connection connection = null;

	private String nome = "postgres";
	private String password = "060705"; // La tua password

	// CORREZIONE 1: Nome database corretto 'progetto_hackathon'
	private String url = "jdbc:postgresql://localhost:5432/HackathonDB";
	private String driver = "org.postgresql.Driver";

	// COSTRUTTORE
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

	public static ConnessioneDatabase getInstance() throws SQLException {
		if (instance == null) {
			instance = new ConnessioneDatabase();
		} else if (instance.connection.isClosed()) {
			instance = new ConnessioneDatabase();
		}
		return instance;
	}

	// CORREZIONE 2: Il metodo che mancava e che il DAO sta cercando!
	public Connection getConnection() {
		return connection;
	}
}