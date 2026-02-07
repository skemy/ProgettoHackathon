package exceptions;

public class AlreadyPartOfATeamException extends Exception {
    public AlreadyPartOfATeamException() {
        super("User is already part of a team.");
    }
}
