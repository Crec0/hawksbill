package club.mindtech.mindbot.commands.poll;

import club.mindtech.mindbot.MindBot;
import club.mindtech.mindbot.commands.BaseCommand;
import club.mindtech.mindbot.database.entities.Poll;
import club.mindtech.mindbot.util.Components;
import club.mindtech.mindbot.util.StrUtil;
import com.mongodb.client.MongoCollection;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public final class CommandPoll extends BaseCommand {

    public CommandPoll() {
        super("poll", "Create a poll", "poll <question> [<option>...]");
    }

    @Override
    public SlashCommandData getCommandData() {
        OptionData[] options = Stream.concat(
            Stream.of(new OptionData(OptionType.STRING, "question", "The question of the poll").setRequired(true)),
            IntStream.range(1, 21).mapToObj(i -> new OptionData(
                OptionType.STRING,
                "option-" + StrUtil.zFill(i, 2),
                "Option %d for the poll".formatted(i)
            ))
        ).toArray(OptionData[]::new);

        SlashCommandData commandData = super.getCommandData();

        commandData.addSubcommands(
            new SubcommandData("create", "Create a poll")
                .addOptions(options),
            new SubcommandData("end", "End the poll")
                .addOptions(new OptionData(OptionType.STRING, "poll-id", "The poll id", true))
        );
        return commandData;
    }

    @Override
    public void onSlashCommand(SlashCommandInteractionEvent event) {
        switch (event.getCommandPath()){
            case "poll/create" -> handlePollCreate(event);
            case "poll/end" -> handlePollEnd(event);
        }
    }

    private void handlePollCreate(SlashCommandInteractionEvent event) {
        List<String> allOptions = event
            .getOptions()
            .stream()
            .map(OptionMapping::getAsString)
            .toList();

        String question = allOptions.get(0);
        List<String> options = allOptions.subList(1, allOptions.size());

        event
            .deferReply()
            .flatMap(hook -> hook.editOriginal(StrUtil.GAP))
            .flatMap(message -> {
                String id = message.getId();
                MessageEmbed embed = new EmbedBuilder()
                    .setTitle(question)
                    .setDescription(getJoinedOptions(options))
                    .setFooter("Poll ID: " + id)
                    .build();
                return message
                    .editMessageEmbeds(embed)
                    .setActionRow(getVoteButton(id, question, options), getRetractButton());
            }).queue(message -> this.createPollEntry(message.getId()));
    }

    private Button getVoteButton(String id, String question, List<String> options) {
        return Components.button(ButtonStyle.PRIMARY, "Vote", (event) -> {
            event.deferReply(true)
                 .setContent("Vote for %s".formatted(question))
                 .addActionRow(getSelecteMenu(id, options))
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

    private SelectMenu getSelecteMenu(String id, List<String> options) {
        return Components.menu(this.getSelectOptions(options), (e) -> {
            if (e instanceof SelectMenuInteractionEvent event) {
                String memberId = event.getUser().getId();
                String selected = event.getSelectedOptions().get(0).getValue();
                this.updatePollEntry(id, memberId, selected);
                e.deferEdit()
                    .setContent("Your vote has been recorded")
                    .setActionRows()
                    .queue();
            }
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
        if (options.size() < 2) {
            return getDefaultOptions();
        }
        return IntStream.range(0, options.size())
            .mapToObj(i -> SelectOption.of("%c".formatted('A' + i), options.get(i)))
            .toList();  
    }

    private String getJoinedOptions(List<String> options) {
        return IntStream.range(0, options.size())
            .mapToObj(i -> ":regional_indicator_%c: ❱❱ ".formatted('a' + i) + options.get(i))
            .collect(Collectors.joining("\n"));
    }

    private MongoCollection<Poll> getCollection() {
        return MindBot.getDatabase().getCollection("polls", Poll.class);
    }

    private void createPollEntry(String messageId) {
        getCollection().insertOne(new Poll(messageId));
    }

    private void updatePollEntry(String voteId, String messageId, String selectedOption) {
        getCollection().updateOne(eq("vote_id", voteId), set("votes."+messageId, selectedOption));
    }

    private void handlePollEnd(SlashCommandInteractionEvent event) {
        String id = Objects.requireNonNull(event.getOption("poll-id")).getAsString();
        Poll poll = getCollection().findOneAndDelete(eq("vote_id", id));

        if (poll == null) {
            event.deferReply(true)
                 .setContent("Poll not found")
                 .queue();
            return;
        }

        StringBuilder result = new StringBuilder().append("Poll results: \n");

        poll.getVotes()
            .entrySet()
            .stream()
            .collect(
                Collectors.groupingBy(Map.Entry::getValue, Collectors.counting())
            )
            .forEach(
                (option, count) -> result.append("%s: %d\n".formatted(option, count))
            );
//https://gist.github.com/kkrypt0nn/a02506f3712ff2d1c8ca7c9e0aed7c06
        // TODO: separate results into a different method and make it pretty
        event.getChannel()
             .editMessageById(id, StrUtil.GAP)
             .content(result.toString())
             .setActionRows()
             .setEmbeds()
             .queue();

        event.deferReply(true)
             .setContent("Poll %s successfully ended".formatted(id))
             .queue();
    }
}
