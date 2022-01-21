package club.mindtech.mindbot.checksAndErrors;

import club.mindtech.mindbot.checksAndErrors.exceptions.MissingRequiredArgumentException;

public class Checks {
    public static <T> T checkNull(T t, String message) {
        if (t == null) {
            throw new MissingRequiredArgumentException(message);
        }
        return t;
    }
}
