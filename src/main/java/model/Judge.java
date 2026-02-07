package model;

public class Judge extends User {

    private int hackathonId;

    public Judge() {
        super();
    }

    public Judge(int userId, String name, String email, String password, int hackathonId) {
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