package club.mindtech.mindbot.commands.ping;

import club.mindtech.mindbot.MindBot;
import club.mindtech.mindbot.commands.BaseCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public final class CommandPing extends BaseCommand {
    public CommandPing() {
        super("ping", "Returns the gateway ping of the user!", "ping");
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        MindBot.getAPI()
               .getRestPing()
               .queue(ping -> event.deferReply().setContent("Pong! " + ping + " ms").queue());
    }
}
