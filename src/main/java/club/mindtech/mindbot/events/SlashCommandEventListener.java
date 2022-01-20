package club.mindtech.mindbot.events;

import club.mindtech.mindbot.MindBot;
import club.mindtech.mindbot.commands.BaseCommand;
import club.mindtech.mindbot.commands.Commands;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class SlashCommandEventListener extends AnnotatedListener {

    @SubscribeEvent
    public void onSlashCommand(SlashCommandEvent event) {
        BaseCommand command = Commands.getCommand(event.getName());
        MindBot.LOGGER.info("Executing command: {} from {}", event.getName(), event.getUser().getName());
        command.onSlashCommand(event);
    }
}
