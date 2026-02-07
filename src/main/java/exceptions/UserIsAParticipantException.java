package exceptions;

public class UserIsAParticipantException extends Exception {
    public UserIsAParticipantException() {
        super("User's role is currently set to Participant.'");
    }
}
