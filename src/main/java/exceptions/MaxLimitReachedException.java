package exceptions;

public class MaxLimitReachedException extends Exception {
    public MaxLimitReachedException() {
        super("Max limit reached.");
    }
}
