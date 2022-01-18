package club.mindtech.mindbot.util;

import club.mindtech.mindbot.config.Config;

public class BotUtil {
    public static String prefix(String name) {
        return Config.PREFIX + name;
    }
}
