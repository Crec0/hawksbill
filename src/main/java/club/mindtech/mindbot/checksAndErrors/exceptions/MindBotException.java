package club.mindtech.mindbot.checksAndErrors.exceptions;

public abstract class MindBotException extends RuntimeException {
    public MindBotException(String message) {
        super(message);
    }
}
