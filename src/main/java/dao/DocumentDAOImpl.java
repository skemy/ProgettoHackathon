package dao;

import database.ConnessioneDatabase;
import model.Document;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementazione dell'interfaccia DocumentDAO per PostgreSQL.
 * Gestisce il ciclo di vita dei documenti caricati dai team, inclusi link a repository e demo.
 */
public class DocumentDAOImpl implements DocumentDAO {

    /**
     * Carica un nuovo link nel database.
     * * @param doc L'oggetto Document da persistere.
     */
    @Override
    public void uploadDocument(Document doc) {
        // Usiamo i nomi corretti del DB: documentLink e description
        String query = "INSERT INTO document (documentLink, description, teamId, uploadDate) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, doc.getUrl());      // documentLink
            ps.setString(2, doc.getName());     // description
            ps.setInt(3, doc.getTeamId());
            ps.setTimestamp(4, Timestamp.valueOf(doc.getUploadDate()));

            ps.executeUpdate();
            System.out.println("✅ Documento salvato per il Team: " + doc.getTeamId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    /**
     * Recupera tutti i documenti associati a un team specifico.
     * * @param teamId L'ID del team di cui recuperare i documenti.
     * @return Una lista di oggetti Document.
     */
    @Override
    public List<Document> getDocumentsByTeam(int teamId) {
        List<Document> docs = new ArrayList<>();
        String query = "SELECT * FROM document WHERE teamId = ?";

        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, teamId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                docs.add(mapResultSetToDocument(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return docs;
    }

    /**
     * Recupera un singolo documento tramite il suo identificativo.
     * * @param documentId L'ID del documento da cercare.
     * @return L'oggetto Document popolato, o null se non trovato.
     */
    @Override
    public Document getDocumentById(int documentId) {
        String query = "SELECT * FROM document WHERE documentId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, documentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToDocument(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Rimuove un documento dal database.
     * * @param documentId L'ID del documento da eliminare.
     */
    @Override
    public void deleteDocument(int documentId) {
        String query = "DELETE FROM document WHERE documentId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, documentId);
            ps.executeUpdate();
            System.out.println("🗑️ Documento eliminato: " + documentId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper per convertire il ResultSet in un oggetto Document.
     * Estrae i dati SQL e li mappa nel modello Java.
     * * @param rs Il ResultSet posizionato sulla riga corrente.
     * @return Un'istanza di Document.
     * @throws SQLException In caso di errore nell'estrazione dei dati.
     */
    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        return new Document(
                rs.getInt("documentId"),
                rs.getString("description"), // Mappato su 'name' nel model
                rs.getString("documentLink"), // Mappato su 'url' nel model
                rs.getTimestamp("uploadDate").toLocalDateTime(),
                rs.getInt("teamId"),
                0 // hackathonId non è presente nella tabella document, usiamo 0
        );
    }
}