package club.mindtech.mindbot.events;

import club.mindtech.mindbot.commands.Commands;
import club.mindtech.mindbot.config.Config;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


public class MessageEventListener {

    @SubscribeEvent
    public void onMessage(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        Pair<String, List<String>> commandArgs = getCommandArgs(event);
        if (commandArgs == null) {
            return;
        }

        Commands.onTextCommand(event, commandArgs.getLeft(), commandArgs.getRight());
    }

    private Pair<String, List<String>> getCommandArgs(MessageReceivedEvent event) {
        String message = event.getMessage().getContentStripped();

        if (message.isEmpty() || message.isBlank() || message.charAt(0) != Config.PREFIX) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(message);
        String command = tokenizer.nextToken().substring(1);

        if (!Commands.isCommand(command)) {
            return null;
        }

        List<String> args = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            args.add(tokenizer.nextToken());
        }
        return new ImmutablePair<>(command, args);
    }
}
