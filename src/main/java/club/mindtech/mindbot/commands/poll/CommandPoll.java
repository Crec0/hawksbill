package club.mindtech.mindbot.commands.poll;

import club.mindtech.mindbot.commands.BaseCommand;
import club.mindtech.mindbot.util.Components;
import club.mindtech.mindbot.util.StrUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CommandPoll extends BaseCommand {

    private String question;
    private List<String> options;
    private Map<String, Integer> votes;

    public CommandPoll() {
        super("poll", "Create a poll", "poll <question> <option> [<option>...]");
        this.question = null;
        this.options = List.of();
        this.votes = new ConcurrentHashMap<>(0);
    }

    @Override
    public SlashCommandData getCommandData() {
        SlashCommandData commandData = super.getCommandData();

        List<OptionData> options = new ArrayList<>(21);

        options.add(new OptionData(OptionType.STRING, "question", "The question of the poll", true));
        for (int i = 1; i < 21; i++) {
            options.add(new OptionData(OptionType.STRING, "option-" + StrUtil.zFill(i, 2), "Option for the poll"));
        }

        commandData.addSubcommands(
            new SubcommandData("create", "Create a poll")
                .addOptions(options.toArray(new OptionData[0])),
            new SubcommandData("end", "End the poll")
                .addOptions(new OptionData(OptionType.STRING, "poll-id", "The poll id", true))
        );
        return commandData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent commandEvent) {
        List<String> allOptions = commandEvent
            .getOptions()
            .stream()
            .map(OptionMapping::getAsString)
            .toList();

        this.question = allOptions.get(0);
        this.options = allOptions.subList(1, allOptions.size());

        commandEvent
            .deferReply()
            .flatMap(hook -> hook.editOriginal(StrUtil.GAP))
            .flatMap(message -> {
                long id = message.getIdLong();
                MessageEmbed embed = new EmbedBuilder()
                    .setTitle(this.question)
                    .setDescription(joinOptions(this.options))
                    .setFooter("Poll ID: " + id)
                    .build();
                return message
                    .editMessageEmbeds(embed)
                    .setActionRow(getVoteButton(), getRetractButton());
            }).queue();
    }

    private Button getVoteButton() {
        return Components.button(ButtonStyle.PRIMARY, "Vote", (event) -> {
            event.deferReply(true)
                 .setContent("Vote for %s".formatted(this.question))
                 .addActionRow(Components.menu(this.getSelectOptions(options)))
                 .queue();
            return false;
        });
    }

    private Button getRetractButton() {
        return Components.button(ButtonStyle.DANGER, "Retract Vote", (event) -> {
            event.deferReply(true)
                 .setContent("Vote removed")
                 .queue();
            return false;
        });
    }

    private List<SelectOption> getDefaultOptions() {
        return List.of(
            SelectOption.of("Upvote", "upvote"),
            SelectOption.of("Downvote", "downvote")
        );
    }

    private List<SelectOption> getSelectOptions(List<String> options) {
        if (this.options.isEmpty()) {
            return getDefaultOptions();
        }
        return IntStream.range(1, options.size())
            .mapToObj(i -> SelectOption.of(StrUtil.zFill(i, 2), options.get(i)))
            .toList();
    }

    private String joinOptions(List<String> options) {
        return IntStream.range(1, options.size())
            .mapToObj(i -> StrUtil.bold(StrUtil.zFill(i, 2)) + ": " + options.get(i))
            .collect(Collectors.joining("\n"));
    }
}
