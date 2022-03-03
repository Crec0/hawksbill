package club.mindtech.mindbot.util;

import club.mindtech.mindbot.config.Config;

import java.util.Set;

public class StrUtil {

    public static String GAP = "⠀";

    public static String prefix(String name) {
        return Config.PREFIX + name;
    }

    public static String bold(String text) {
        return "**" + text.strip() + "**";
    }

    public static String italic(String text) {
        return "*" + text.strip() + "*";
    }

    public static String underline(String text) {
        return "__" + text.strip() + "__";
    }

    public static String code(String text) {
        return "`" + text.strip() + "`";
    }

    public static String stringify(Set<String> strings) {
        return strings.toString().replace("[", "").replace("]", "");
    }

    public static String zFill(int number, int length) {
        return String.format("%0" + length + "d", number);
    }
}
