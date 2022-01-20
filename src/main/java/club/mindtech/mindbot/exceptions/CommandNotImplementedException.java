package club.mindtech.mindbot.exceptions;

public class CommandNotImplementedException extends RuntimeException {
    public CommandNotImplementedException(String message) {
        super(message);
    }
}
