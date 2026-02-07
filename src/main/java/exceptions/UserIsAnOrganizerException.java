package exceptions;

public class UserIsAnOrganizerException extends Exception {
    public UserIsAnOrganizerException() {
        super("User's role is currently set to Organizer.");
    }
}
