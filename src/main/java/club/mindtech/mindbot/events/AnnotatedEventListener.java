package club.mindtech.mindbot.events;

import club.mindtech.mindbot.MindBot;
import club.mindtech.mindbot.commands.BaseCommand;
import club.mindtech.mindbot.commands.Commands;
import club.mindtech.mindbot.exceptions.MindBotException;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public final class AnnotatedEventListener {
	private static final Map<String, ComponentCallback> componentCallbacks = new HashMap<>();

	public static void awaitEvent(String id, ComponentCallback function) {
		componentCallbacks.put(id, function);
	}

	@SubscribeEvent
	public void onComponent(GenericComponentInteractionCreateEvent event) {
		String id = event.getComponentId();
		if (componentCallbacks.containsKey(id)) {
			boolean shouldRemove = componentCallbacks.get(id).call(event);
			if (shouldRemove) {
				componentCallbacks.remove(id);
			}
		}
	}

	@SubscribeEvent
	public void onReady(ReadyEvent event) {
		MindBot.LOGGER.info("Bot is ready!");
	}

	@SubscribeEvent
	public void onSlashCommand(SlashCommandInteractionEvent event) {
		BaseCommand command = Commands.getCommand(event.getName());
		MindBot.LOGGER.info("Executing command: {} from {}", event.getName(), event.getUser().getName());
		try {
			command.onSlashCommand(event);
		} catch (MindBotException e) {
			event.deferReply(true).setContent(e.getMessage()).queue();
		}
	}
}
