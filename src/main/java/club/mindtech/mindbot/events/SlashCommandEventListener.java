package club.mindtech.mindbot.events;

import club.mindtech.mindbot.commands.Commands;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class SlashCommandEventListener {

    @SubscribeEvent
    public void onSlashCommand(SlashCommandEvent event) {
        Commands.getCommand(event.getName()).onSlashCommand(event);
    }
}
