package model;

import java.time.LocalDateTime;

/**
 * Rappresenta l'entità di dominio Document.
 * Modella una risorsa (es. link a repository, demo) caricata da un team per un hackathon.
 * <p>
 * Nel rispetto del pattern BCE, questa classe opera esclusivamente nel layer Entity:
 * è un puro POJO (Plain Old Java Object) e non contiene logica di validazione o persistenza.
 */
public class Document {

    private int documentId;
    private String name;
    private String url;
    private LocalDateTime uploadDate;
    private int teamId;
    private int hackathonId; // FIX: Campo precedentemente mancante

    /**
     * Costruttore vuoto, necessario per le istanziazioni tramite reflection
     * o framework di persistenza/DAO.
     */
    public Document() {
    }

    /**
     * Costruttore completo per l'inizializzazione dell'entità.
     *
     * @param documentId  L'ID univoco del documento.
     * @param name        Il nome o la descrizione del documento.
     * @param url         L'indirizzo URL della risorsa esterna.
     * @param uploadDate  La data e l'ora del caricamento.
     * @param teamId      L'ID del team proprietario del documento.
     * @param hackathonId L'ID dell'hackathon a cui il documento fa riferimento.
     */
    public Document(int documentId, String name, String url,
                    LocalDateTime uploadDate, int teamId, int hackathonId) {
        this.documentId = documentId;
        this.name = name;
        this.url = url;
        this.uploadDate = uploadDate;
        this.teamId = teamId;
        this.hackathonId = hackathonId;
    }

    /**
     * @return L'ID univoco del documento.
     */
    public int getDocumentId() { return documentId; }

    /**
     * @param documentId L'ID univoco da assegnare al documento.
     */
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    /**
     * @return Il nome o la descrizione associata.
     */
    public String getName() { return name; }

    /**
     * @param name Il nome da assegnare al documento.
     */
    public void setName(String name) { this.name = name; }

    /**
     * @return L'indirizzo URL della risorsa.
     */
    public String getUrl() { return url; }

    /**
     * @param url L'indirizzo URL da salvare.
     */
    public void setUrl(String url) { this.url = url; }

    /**
     * @return Il timestamp esatto del caricamento.
     */
    public LocalDateTime getUploadDate() { return uploadDate; }

    /**
     * @param uploadDate Il timestamp del caricamento.
     */
    public void setUploadDate(LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

    /**
     * @return L'ID del team proprietario.
     */
    public int getTeamId() { return teamId; }

    /**
     * @param teamId L'ID del team da associare.
     */
    public void setTeamId(int teamId) { this.teamId = teamId; }

    /**
     * @return L'ID dell'hackathon associato.
     */
    public int getHackathonId() { return hackathonId; }

    /**
     * @param hackathonId L'ID dell'hackathon da associare.
     */
    public void setHackathonId(int hackathonId) { this.hackathonId = hackathonId; }
}