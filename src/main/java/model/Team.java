package model;

import java.time.LocalDateTime;

public class Team {

    private int teamId;
    private String teamName;
    private String accessCode;
    private LocalDateTime creationDate;
    private int hackathonId;

    public Team() {}

    public Team(int teamId, String teamName, String accessCode, LocalDateTime creationDate, int hackathonId) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.accessCode = accessCode;
        this.creationDate = creationDate;
        this.hackathonId = hackathonId;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public int getHackathonId() {
        return hackathonId;
    }

    public void setHackathonId(int hackathonId) {
        this.hackathonId = hackathonId;
    }
}