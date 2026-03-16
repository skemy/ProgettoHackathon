package model;

import java.time.LocalDateTime;

/**
 * Rappresenta l'entità di dominio Hackathon.
 * Questa classe fa parte del layer Model (Entity) nell'architettura BCE.
 * <p>
 * Per risolvere l'eccessiva complessità del costruttore (Long Parameter List),
 * l'istanziazione di questa classe è gestita tramite il Builder Pattern.
 */
public class Hackathon {

    private int hackathonId;
    private String title;
    private String location;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime registrationStartDate;
    private LocalDateTime registrationEndDate;
    private int maxParticipants;
    private int maxTeamSize;
    private String problemDescription;

    /**
     * Costruttore vuoto necessario per i framework di persistenza o per
     * l'istanziazione tramite reflection.
     */
    public Hackathon() {
    }

    /**
     * Costruttore privato utilizzato esclusivamente dal Builder.
     * * @param builder L'oggetto Builder contenente i dati precompilati.
     */
    private Hackathon(Builder builder) {
        this.hackathonId = builder.hackathonId;
        this.title = builder.title;
        this.location = builder.location;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.registrationStartDate = builder.registrationStartDate;
        this.registrationEndDate = builder.registrationEndDate;
        this.maxParticipants = builder.maxParticipants;
        this.maxTeamSize = builder.maxTeamSize;
        this.problemDescription = builder.problemDescription;
    }

    // --- GETTERS & SETTERS ---

    public int getHackathonId() { return hackathonId; }
    public void setHackathonId(int hackathonId) { this.hackathonId = hackathonId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public LocalDateTime getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(LocalDateTime registrationStartDate) { this.registrationStartDate = registrationStartDate; }

    public LocalDateTime getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(LocalDateTime registrationEndDate) { this.registrationEndDate = registrationEndDate; }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }

    public int getMaxTeamSize() { return maxTeamSize; }
    public void setMaxTeamSize(int maxTeamSize) { this.maxTeamSize = maxTeamSize; }

    public String getProblemDescription() { return problemDescription; }
    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription; }

    // --- BUILDER PATTERN ---

    /**
     * Classe statica interna che implementa il Builder Pattern per l'entità Hackathon.
     * Garantisce una creazione sicura, leggibile e flessibile dell'oggetto.
     */
    public static class Builder {
        private int hackathonId;
        private String title;
        private String location;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime registrationStartDate;
        private LocalDateTime registrationEndDate;
        private int maxParticipants;
        private int maxTeamSize;
        private String problemDescription;

        public Builder hackathonId(int hackathonId) {
            this.hackathonId = hackathonId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder location(String location) {
            this.location = location;
            return this;
        }

        public Builder startDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder endDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder registrationStartDate(LocalDateTime registrationStartDate) {
            this.registrationStartDate = registrationStartDate;
            return this;
        }

        public Builder registrationEndDate(LocalDateTime registrationEndDate) {
            this.registrationEndDate = registrationEndDate;
            return this;
        }

        public Builder maxParticipants(int maxParticipants) {
            this.maxParticipants = maxParticipants;
            return this;
        }

        public Builder maxTeamSize(int maxTeamSize) {
            this.maxTeamSize = maxTeamSize;
            return this;
        }

        public Builder problemDescription(String problemDescription) {
            this.problemDescription = problemDescription;
            return this;
        }

        /**
         * Finalizza la costruzione dell'oggetto.
         * * @return Una nuova istanza validata di Hackathon.
         */
        public Hackathon build() {
            return new Hackathon(this);
        }
    }
}