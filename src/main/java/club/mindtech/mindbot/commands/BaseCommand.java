package club.mindtech.mindbot.commands;

import club.mindtech.mindbot.util.StrUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class BaseCommand {
    private final String name;
    private final String description;
    private final String usage;

    public BaseCommand(String name, String description, String usage) {
        this.name = name;
        this.description = description;
        this.usage = StrUtil.prefix(usage);
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

    public SlashCommandData getCommandData() {
        return Commands.slash(name, description);
    }

    public void onSlashCommand(SlashCommandInteractionEvent event) {}
}
