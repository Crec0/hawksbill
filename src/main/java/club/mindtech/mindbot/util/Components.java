package club.mindtech.mindbot.util;

import club.mindtech.mindbot.events.AnnotatedEventListener;
import club.mindtech.mindbot.events.ComponentCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;
import java.util.UUID;

public class Components {

    private static String getId() {
        return UUID.randomUUID().toString();
    }

    public static Button button(ButtonStyle style, String label, ComponentCallback callback) {
        String id = getId();
        AnnotatedEventListener.awaitEvent(id, callback);
        return Button.of(style, id, label);
    }

    public static SelectMenu menu(List<SelectOption> options, ComponentCallback callback) {
        String id = getId();
        AnnotatedEventListener.awaitEvent(id, callback);
        return SelectMenu
            .create(id)
            .addOptions(options)
            .build();
    }
}
