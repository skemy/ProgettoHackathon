package dao;

import database.ConnessioneDatabase;
import model.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia DocumentDAO per PostgreSQL.
 * Gestisce il ciclo di vita dei documenti (link, repository, demo) caricati dai team.
 * <p>
 * Nota Architetturale: Questa classe racchiude la logica di accesso ai dati.
 * Utilizza blocchi try-with-resources per garantire il rilascio automatico e sicuro
 * di Connessioni, Statement e ResultSet, prevenendo Resource Leaks (standard SonarQube).
 */
public class DocumentDAOImpl implements DocumentDAO {

    private static final Logger LOGGER = Logger.getLogger(DocumentDAOImpl.class.getName());

    @Override
    public void uploadDocument(Document doc) {
        String query = "INSERT INTO document (documentLink, description, teamId, uploadDate) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, doc.getUrl());      // Mappato su documentLink
            ps.setString(2, doc.getName());     // Mappato su description
            ps.setInt(3, doc.getTeamId());
            ps.setTimestamp(4, Timestamp.valueOf(doc.getUploadDate()));

            ps.executeUpdate();
            LOGGER.log(Level.INFO, "Documento salvato con successo per il Team ID: {0}", doc.getTeamId());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'upload del documento", e);
        }
    }

    @Override
    public List<Document> getDocumentsByTeam(int teamId) {
        List<Document> docs = new ArrayList<>();
        String query = "SELECT * FROM document WHERE teamId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            // FIX SonarQube: Inserimento del ResultSet nel try-with-resources per evitare Memory Leaks
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    docs.add(mapResultSetToDocument(rs));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero dei documenti per il Team ID: " + teamId, e);
        }
        return docs;
    }

    @Override
    public Document getDocumentById(int documentId) {
        String query = "SELECT * FROM document WHERE documentId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, documentId);
            // FIX SonarQube: ResultSet protetto per rilascio risorse automatico
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore nel recupero del documento ID: " + documentId, e);
        }
        return null;
    }

    @Override
    public void deleteDocument(int documentId) {
        String query = "DELETE FROM document WHERE documentId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, documentId);
            ps.executeUpdate();
            LOGGER.log(Level.INFO, "Documento eliminato con successo. ID: {0}", documentId);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Errore durante l'eliminazione del documento ID: " + documentId, e);
        }
    }

    /**
     * Helper privato per la conversione (Object-Relational Mapping manuale).
     * Estrae i dati SQL e li mappa nel modello Java.
     *
     * @param rs Il ResultSet posizionato sulla riga corrente.
     * @return Un'istanza di Document popolata.
     * @throws SQLException In caso di errore di lettura dal database.
     */
    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        return new Document(
                rs.getInt("documentId"),
                rs.getString("description"),
                rs.getString("documentLink"),
                rs.getTimestamp("uploadDate").toLocalDateTime(),
                rs.getInt("teamId"),
                0 // Nota Architetturale: La tabella non traccia l'hackathonId direttamente, impostiamo un default neutrale.
        );
    }
}