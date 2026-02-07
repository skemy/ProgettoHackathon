package model;

public class Participant extends User {

    private int teamId;
    private String role;

    public Participant() {
        super();
    }

    public Participant(int userId, String name, String email, String password, int teamId, String role) {
        super(userId, name, email, password);
        this.teamId = teamId;
        this.role = role;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}