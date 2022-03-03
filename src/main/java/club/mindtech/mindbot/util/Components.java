package club.mindtech.mindbot.util;

import club.mindtech.mindbot.events.AnnotatedEventListener;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Components {

    private static final AtomicInteger CURRENT_ID = new AtomicInteger(0);

    private static String getId() {
        return "" + CURRENT_ID.getAndIncrement();
    }

    public static Button button(ButtonStyle style, String label, ComponentCallback callback) {
        String id = getId();
        AnnotatedEventListener.awaitEvent(id, callback);
        return Button.of(style, id, label);
    }

    public static SelectMenu menu(List<SelectOption> options) {
        return SelectMenu
            .create(getId())
            .addOptions(options)
            .build();
    }
}
