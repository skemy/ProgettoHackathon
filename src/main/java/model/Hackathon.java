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
    public void setHackathonId(int hackathonId) {
        if (hackathonId < 0) throw new IllegalArgumentException("Hackathon ID cannot be negative.");
        this.hackathonId = hackathonId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title cannot be null or empty.");
        this.title = title;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) throw new IllegalArgumentException("Location cannot be null or empty.");
        this.location = location;
    }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) {
        if (startDate == null) throw new IllegalArgumentException("Start date cannot be null.");
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) {
        if (endDate == null) throw new IllegalArgumentException("End date cannot be null.");
        this.endDate = endDate;
    }

    public LocalDateTime getRegistrationStartDate() { return registrationStartDate; }
    public void setRegistrationStartDate(LocalDateTime registrationStartDate) {
        if (registrationStartDate == null) throw new IllegalArgumentException("Registration start date cannot be null.");
        this.registrationStartDate = registrationStartDate;
    }

    public LocalDateTime getRegistrationEndDate() { return registrationEndDate; }
    public void setRegistrationEndDate(LocalDateTime registrationEndDate) {
        if (registrationEndDate == null) throw new IllegalArgumentException("Registration end date cannot be null.");
        this.registrationEndDate = registrationEndDate;
    }

    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) {
        if (maxParticipants <= 0) throw new IllegalArgumentException("Max participants must be greater than zero.");
        this.maxParticipants = maxParticipants;
    }

    public int getMaxTeamSize() { return maxTeamSize; }
    public void setMaxTeamSize(int maxTeamSize) {
        if (maxTeamSize <= 0) throw new IllegalArgumentException("Max team size must be greater than zero.");
        this.maxTeamSize = maxTeamSize;
    }

    public String getProblemDescription() { return problemDescription; }

    public void setProblemDescription(String problemDescription) { this.problemDescription = problemDescription;
    }

    /**
     * Verifica se l'evento è ufficialmente iniziato in base al timestamp di sistema.
     * <p>
     * Questo stato determina l'apertura delle sottomissioni per i team e
     * l'attivazione della fase di valutazione per i giudici.
     * </p>
     * * @return {@code true} se la data corrente è uguale o successiva a {@code startDate}
     */
    public boolean isStarted() {
        return !LocalDateTime.now().isBefore(this.startDate);
    }

    /**
     * Verifica se l'evento è ufficialmente concluso.
     * @return true se l'ora attuale è successiva alla data di fine.
     */
    public boolean isEnded() {
        return LocalDateTime.now().isAfter(this.endDate);
    }
    // --- BUILDER PATTERN ---

    /**
     * Finalizza la costruzione dell'oggetto Hackathon applicando le regole di validazione atomica.
     * <p>
     * Assicura che i campi obbligatori (Titolo, Location, Date e Limiti) siano
     * presenti e coerenti prima di restituire l'istanza.
     * </p>
     * * @return una nuova istanza validata di {@link Hackathon}
     * @throws IllegalArgumentException se i requisiti minimi di integrità non sono soddisfatti
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
         * Applica la validazione atomica per garantire la coerenza dei dati obbligatori.
         * * @return Una nuova istanza validata di Hackathon.
         */
        public Hackathon build() {
            if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title is required.");
            if (location == null || location.trim().isEmpty()) throw new IllegalArgumentException("Location is required.");
            if (startDate == null || endDate == null) throw new IllegalArgumentException("Dates are required.");
            if (maxParticipants <= 0 || maxTeamSize <= 0) throw new IllegalArgumentException("Limits must be greater than zero.");

            return new Hackathon(this);
        }
    }
}