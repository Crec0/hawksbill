package club.mindtech.mindbot.util;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

@FunctionalInterface
public interface ComponentCallback {
	boolean call(GenericComponentInteractionCreateEvent event);
}
