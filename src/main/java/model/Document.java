package model;

import java.time.LocalDateTime;

/**
 * Rappresenta un documento o una risorsa caricata da un team per un hackathon.
 * Può includere link a repository, demo o presentazioni.
 */
public class Document {

    private int documentId;
    private String name;
    private String url;
    private LocalDateTime uploadDate;
    private int teamId;

    /**
     * Costruttore vuoto della classe Document.
     */
    public Document() {}

    /**
     * Costruttore completo della classe Document.
     * * @param documentId L'ID univoco del documento.
     * @param name Il nome o la descrizione del documento.
     * @param url L'indirizzo URL della risorsa.
     * @param uploadDate La data e l'ora del caricamento.
     * @param teamId L'ID del team che ha caricato il documento.
     * @param hackathonId L'ID dell'hackathon di riferimento.
     */
    public Document(int documentId, String name, String url,
                    LocalDateTime uploadDate, int teamId, int hackathonId) {
        this.documentId = documentId;
        this.name = name;
        this.url = url;
        this.uploadDate = uploadDate;
        this.teamId = teamId;
    }

    /**
     * @return L'ID univoco del documento.
     */
    public int getDocumentId() {
        return documentId;
    }

    /**
     * @param documentId L'ID univoco da assegnare al documento.
     */
    public void setDocumentId(int documentId) {
        this.documentId = documentId;
    }

    /**
     * @return Il nome o la descrizione del documento.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Il nome da assegnare al documento.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return L'URL della risorsa.
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url L'URL da assegnare al documento.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return La data e l'ora di caricamento.
     */
    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    /**
     * @param uploadDate La data e l'ora da impostare per il caricamento.
     */
    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * @return L'ID del team associato.
     */
    public int getTeamId() {
        return teamId;
    }

    /**
     * @param teamId L'ID del team da associare al documento.
     */
    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

}