package exceptions;

public class UserIsAJudgeException extends Exception {
    public UserIsAJudgeException() {
        super("User's role is currently set to Judge.");
    }
}
