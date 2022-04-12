package club.mindtech.mindbot.events;

import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;


public interface ComponentCallback {
	boolean call(GenericComponentInteractionCreateEvent event);
}
