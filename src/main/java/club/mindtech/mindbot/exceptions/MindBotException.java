package club.mindtech.mindbot.exceptions;

public abstract class MindBotException extends RuntimeException {
    public MindBotException(String message) {
        super(message);
    }
}
