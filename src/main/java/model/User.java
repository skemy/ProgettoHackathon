package model;

public class User {
    private int userId;
    private String name;
    private String email;
    private String password;

    public User (){}

    //costruttore per le registrazioni
    public User (String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // costruttore per il DB
    public User(int userId, String name, String email, String password) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    // Utile per il debug
    @Override
    public String toString() {
        return "User [id=" + userId + ", name=" + name + ", email=" + email + "]";
    }
}
