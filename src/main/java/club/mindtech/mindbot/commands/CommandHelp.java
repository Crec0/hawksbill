package club.mindtech.mindbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public class CommandHelp extends BaseCommand {

    public CommandHelp() {
        super("help", "Shows information about other commands", "help [name]", "h");
    }

    @Override
    public boolean isSlashCommand() {
        return true;
    }

    @Override
    public CommandData getCommandData() {
        CommandData data = super.getCommandData();
        data.addOption(OptionType.STRING, "command", "Command name the help should be shown for", false);
        return data;
    }

    @Override
    public void onCommand(MessageReceivedEvent event, List<String> args) {
        super.onCommand(event, args);
        BaseCommand embedFor = this;

        if (args.size() > 0 && Commands.isCommand(args.get(0))) {
            embedFor = Commands.getCommand(args.get(0));
        }

        event.getMessage()
             .replyEmbeds(getHelpEmbed(event.getAuthor(), embedFor))
             .queue();
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        super.onSlashCommand(event);

        BaseCommand embedFor = this;
        OptionMapping mapping = event.getOption("command");

        if (mapping != null) {
            String commandName = mapping.getAsString();
            if (Commands.isCommand(commandName)) {
                embedFor = Commands.getCommand(commandName);
            }
        }

        event.deferReply()
             .addEmbeds(getHelpEmbed(event.getUser(), embedFor))
             .queue();
    }

    private static MessageEmbed getHelpEmbed(User author, BaseCommand command) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(command.getName())
                .setDescription(command.getDescription())
                .addField("Usage", command.getUsage(), true)
                .addField("Aliases", command.getAliases().toString(), true)
                .setColor(0x00f200)
                .setFooter(author.getAsTag());
        return builder.build();
    }
}
