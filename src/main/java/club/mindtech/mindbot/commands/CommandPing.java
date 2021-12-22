package club.mindtech.mindbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class CommandPing extends BaseCommand {
    public CommandPing() {
        super("ping", "Pings the user back.", "ping", "p");
    }

    @Override
    public void onCommand(MessageReceivedEvent event, List<String> args) {
        super.onCommand(event, args);
        event.getMessage().reply("Pong!").queue();
    }
}
