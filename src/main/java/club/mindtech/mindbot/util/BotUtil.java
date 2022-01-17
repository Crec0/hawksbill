package club.mindtech.mindbot.util;

import club.mindtech.mindbot.config.Config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BotUtil {

    public static String prefix(String name) {
        return Config.PREFIX + name;
    }

    public static Set<String> asCombinedSet(String name, String... aliases) {
        Set<String> set = new HashSet<>();
        set.add(name);
        Collections.addAll(set, aliases);
        return set;
    }
}
