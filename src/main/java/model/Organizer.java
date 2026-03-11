package model;

public class Organizer extends User {

    private int hackathonId;

    public Organizer(int userId, String name, String email, String password, int hackathonId) {
        super(userId, name, email, password); // Passa i dati dell'utente alla classe padre (User)
        this.hackathonId = hackathonId;       // Salva FINALMENTE l'ID dell'hackathon!
    }

    public int getHackathonId() {
        return hackathonId;
    }

    public void setHackathonId(int hackathonId) {
        this.hackathonId = hackathonId;
    }
}