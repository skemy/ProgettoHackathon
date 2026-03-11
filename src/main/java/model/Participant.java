package model;

public class Participant extends User {

    private int teamId;
    private String role;

    // 1. COSTRUTTORE A 5 PARAMETRI (Quello usato dal tuo UserDAOImpl!)
    public Participant(int id, String name, String email, String password, int teamId) {
        super(id, name, email, password); // Passa i dati alla classe padre User
        this.teamId = teamId;             // Salva il Team ID
        // Valore di default se non specificato
    }


    // --- GETTER & SETTER ---
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