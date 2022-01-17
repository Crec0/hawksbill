package club.mindtech.mindbot.helpers;

import java.util.Set;

public class StringHelper {

    public static String GAP = "⠀";

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
}
