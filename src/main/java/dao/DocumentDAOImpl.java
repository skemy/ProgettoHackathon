package dao;

import database.ConnessioneDatabase;
import model.Document;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementazione dell'interfaccia DocumentDAO per la gestione dei documenti nel database.
 * Questa classe fornisce metodi per caricare, recuperare, ottenere e eliminare documenti associati a team specifici.
 */
public class DocumentDAOImpl implements DocumentDAO {
    private static final Logger LOGGER = Logger.getLogger(DocumentDAOImpl.class.getName());

    /**
     * Carica un documento nel database.
     * @param doc Il documento da caricare.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public void uploadDocument(Document doc) throws SQLException {
        String query = "INSERT INTO document (documentLink, description, teamId, uploadDate) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, doc.getUrl());
            ps.setString(2, doc.getName());
            ps.setInt(3, doc.getTeamId());
            ps.setTimestamp(4, Timestamp.valueOf(doc.getUploadDate()));
            ps.executeUpdate();
            LOGGER.log(Level.INFO, "Document uploaded for Team ID: {0}", doc.getTeamId());
        }
    }

    /**
     * Recupera una lista di documenti associati a un team specifico.
     * @param teamId L'ID del team per cui recuperare i documenti.
     * @return Una lista di documenti del team.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public List<Document> getDocumentsByTeam(int teamId) throws SQLException {
        List<Document> docs = new ArrayList<>();
        String query = "SELECT documentId, description, documentLink, uploadDate, teamId FROM document WHERE teamId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    docs.add(new Document(rs.getInt("documentId"), rs.getString("description"), rs.getString("documentLink"),
                            rs.getTimestamp("uploadDate").toLocalDateTime(), rs.getInt("teamId"), 0));
                }
            }
        }
        return docs;
    }

    /**
     * Recupera un documento specifico tramite il suo ID.
     * @param documentId L'ID del documento da recuperare.
     * @return Il documento corrispondente all'ID, o null se non trovato.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public Document getDocumentById(int documentId) throws SQLException {
        String query = "SELECT documentId, description, documentLink, uploadDate, teamId FROM document WHERE documentId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, documentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Document(rs.getInt("documentId"), rs.getString("description"), rs.getString("documentLink"),
                            rs.getTimestamp("uploadDate").toLocalDateTime(), rs.getInt("teamId"), 0);
                }
            }
        }
        return null;
    }

    /**
     * Elimina un documento dal database tramite il suo ID.
     * @param documentId L'ID del documento da eliminare.
     * @throws SQLException Se si verifica un errore durante l'operazione di database.
     */
    @Override
    public void deleteDocument(int documentId) throws SQLException {
        String query = "DELETE FROM document WHERE documentId = ?";
        try (Connection conn = ConnessioneDatabase.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, documentId);
            ps.executeUpdate();
        }
    }
}