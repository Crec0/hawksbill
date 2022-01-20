package club.mindtech.mindbot.commands;

import club.mindtech.mindbot.MindBot;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

public class CommandPing extends BaseCommand {
    public CommandPing() {
        super("ping", "Returns the gateway ping of the user!", "ping");
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        MindBot.getAPI()
               .getRestPing()
               .queue(ping -> event.deferReply().setContent("Pong! " + ping + " ms").queue());
    }
}
