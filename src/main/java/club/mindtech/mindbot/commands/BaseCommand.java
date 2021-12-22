package club.mindtech.mindbot.commands;

import club.mindtech.mindbot.MindBot;
import club.mindtech.mindbot.util.BotUtil;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;
import java.util.Set;

public abstract class BaseCommand {
    private final String name;
    private final String description;
    private final String usage;
    private final Set<String> aliases;

    public BaseCommand(String name, String description, String usage, String... aliases) {
        this.name = name;
        this.description = description;
        this.usage = BotUtil.prefix(usage);
        this.aliases = BotUtil.asCombinedSet(name, aliases);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public void addAlias(String alias) {
        aliases.add(alias);
        Commands.registerAlias(alias, this);
    }

    public CommandData getCommandData() {
        return new CommandData(name, description);
    }

    public boolean isSlashCommand() {
        return false;
    }

    public void onCommand(MessageReceivedEvent event, List<String> args) {
        MindBot.LOGGER.info("Executing: {}, By: {}", getName(), event.getAuthor().getName());
    }

    public void onSlashCommand(SlashCommandEvent event) {
        if (isSlashCommand()) {
            MindBot.LOGGER.info("Executing: {}, By: {}", getName(), event.getUser().getName());
        }
    }
}
