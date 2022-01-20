package club.mindtech.mindbot.commands;

import club.mindtech.mindbot.exceptions.CommandNotImplementedException;
import club.mindtech.mindbot.util.BotUtil;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public abstract class BaseCommand {
    private final String name;
    private final String description;
    private final String usage;

    public BaseCommand(String name, String description, String usage) {
        this.name = name;
        this.description = description;
        this.usage = BotUtil.prefix(usage);
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

    public CommandData getCommandData() {
        return new CommandData(name, description);
    }

    public void onSlashCommand(SlashCommandEvent event) {
        throw new CommandNotImplementedException("This command does not support slash commands.");
    }
}
