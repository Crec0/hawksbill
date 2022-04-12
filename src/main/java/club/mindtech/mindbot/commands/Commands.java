package club.mindtech.mindbot.commands;

import club.mindtech.mindbot.commands.help.CommandHelp;
import club.mindtech.mindbot.commands.ping.CommandPing;
import club.mindtech.mindbot.commands.poll.CommandPoll;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Map;
import java.util.TreeMap;

public class Commands {

    private static final Map<String, BaseCommand> COMMANDS = new TreeMap<>();

    public static void register(BaseCommand command) {
        COMMANDS.put(command.getName(), command);
    }

    private static void registerCommands() {
        register(new CommandPing());
        register(new CommandPoll());
        register(new CommandHelp());
    }

    public static CommandData[] getSlashCommandData() {
        registerCommands();
        return COMMANDS
                .values()
                .stream()
                .map(BaseCommand::getCommandData)
                .toArray(CommandData[]::new);
    }

    public static boolean isCommand(String command) {
        return COMMANDS.containsKey(command);
    }

    public static BaseCommand getCommand(String command) {
        return COMMANDS.get(command);
    }
}
