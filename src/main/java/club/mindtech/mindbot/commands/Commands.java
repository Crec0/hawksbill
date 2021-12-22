package club.mindtech.mindbot.commands;

import club.mindtech.mindbot.util.BotUtil;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Commands {

    private static final Map<String, BaseCommand> MAIN_COMMANDS = new TreeMap<>();
    private static final Map<String, BaseCommand> ALIAS_COMMANDS = new TreeMap<>();

    private static List<BaseCommand> getAllCommandInstances() {
        return BotUtil.CommandList.of(
                new CommandPing(),
                new CommandHelp()
        );
    }

    public static void registerMain(BaseCommand command) {
        MAIN_COMMANDS.put(command.getName(), command);
    }

    public static void registerAlias(String alias, BaseCommand command) {
        ALIAS_COMMANDS.put(alias, command);
    }

    private static void registerCommands() {
        for (BaseCommand command : getAllCommandInstances()) {
            registerMain(command);

            for (String alias : command.getAliases()) {
                registerAlias(alias, command);
            }
        }
    }

    public static CommandData[] getSlashCommandData() {
        registerCommands();
        return MAIN_COMMANDS
                .values()
                .stream()
                .filter(BaseCommand::isSlashCommand)
                .map(BaseCommand::getCommandData)
                .toArray(CommandData[]::new);
    }

    public static boolean isCommand(String command) {
        return ALIAS_COMMANDS.containsKey(command);
    }

    public static BaseCommand getCommand(String command) {
        return ALIAS_COMMANDS.get(command);
    }

    public static void onTextCommand(MessageReceivedEvent event, String commandName, List<String> args) {
        ALIAS_COMMANDS.get(commandName).onCommand(event, args);
    }
}
