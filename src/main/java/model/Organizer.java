package model;

public class Organizer extends User {


    private int hackathonId;

    public Organizer() {
        super();
    }

    public Organizer(int userId, String name, String email, String password, int organizerId, int hackathonId) {
        super(userId, name, email, password);
        this.hackathonId = hackathonId;
    }


    public int getHackathonId() {
        return hackathonId;
    }

    public void setHackathonId(int hackathonId) {
        this.hackathonId = hackathonId;
    }
}