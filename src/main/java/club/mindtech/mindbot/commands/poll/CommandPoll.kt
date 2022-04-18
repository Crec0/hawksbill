package club.mindtech.mindbot.commands.poll

import club.mindtech.mindbot.commands.BaseCommand
import club.mindtech.mindbot.util.zFill
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

class CommandPoll : BaseCommand("poll", "Create a poll", "poll <question> [<option>...]") {
    override fun getCommandData(): SlashCommandData {
        val options = Stream.concat(
            Stream.of(OptionData(OptionType.STRING, "question", "The question of the poll").setRequired(true)),
            IntStream.range(1, 21).mapToObj { i: Int ->
                OptionData(
                    OptionType.STRING,
                    "option-${zFill(i, 2)}",
                    "Option $i for the poll"
                )
            }
        ).toArray { arrayOfNulls<OptionData>(it) }
        val commandData = super.getCommandData()
        commandData.addSubcommands(
            SubcommandData("create", "Create a poll")
                .addOptions(*options),
            SubcommandData("end", "End the poll")
                .addOptions(OptionData(OptionType.STRING, "poll-id", "The poll id", true))
        )
        return commandData
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        when (event.commandPath) {
            "poll/create" -> handlePollCreate(event)
            "poll/end" -> handlePollEnd(event)
        }
    }

    private fun handlePollCreate(event: SlashCommandInteractionEvent) {
//        List<String> allOptions = event
//            .getOptions()
//            .stream()
//            .map(OptionMapping::getAsString)
//            .toList();
//
//        String question = allOptions.get(0);
//        List<String> options = allOptions.subList(1, allOptions.size());
//
//        event
//            .deferReply()
//            .flatMap(hook -> hook.editOriginal(StrUtil.gap))
//            .flatMap(message -> {
//                String id = message.getId();
//                MessageEmbed embed = new EmbedBuilder()
//                    .setTitle(question)
//                    .setDescription(getJoinedOptions(options))
//                    .setFooter("Poll ID: " + id)
//                    .build();
//                return message
//                    .editMessageEmbeds(embed)
//                    .setActionRow(getVoteButton(id, question, options), getRetractButton());
//            }).queue(message -> this.createPollEntry(message.getId()));
    }

    //    private Button getVoteButton(String id, String question, List<String> options) {
    //        return Components.button(ButtonStyle.PRIMARY, "Vote", (event) -> {
    //            event.deferReply(true)
    //                 .setContent("Vote for %s".formatted(question))
    //                 .addActionRow(getSelecteMenu(id, options))
    //                 .queue();
    //            return false;
    //        });
    //    }
    //
    //    private Button getRetractButton() {
    //        return Components.button(ButtonStyle.DANGER, "Retract Vote", (event) -> {
    //            event.deferReply(true)
    //                 .setContent("Vote removed")
    //                 .queue();
    //            return false;
    //        });
    //    }
    //
    //    private SelectMenu getSelecteMenu(String id, List<String> options) {
    //        return Components.menu(this.getSelectOptions(options), (e) -> {
    //            if (e instanceof SelectMenuInteractionEvent event) {
    //                String memberId = event.getUser().getId();
    //                String selected = event.getSelectedOptions().get(0).getValue();
    //                this.updatePollEntry(id, memberId, selected);
    //                e.deferEdit()
    //                    .setContent("Your vote has been recorded")
    //                    .setActionRows()
    //                    .queue();
    //            }
    //            return false;
    //        });
    //    }
    val defaultOptions: List<SelectOption>
        get() = listOf(
            SelectOption.of("Upvote", "upvote"),
            SelectOption.of("Downvote", "downvote")
        )

    private fun getSelectOptions(options: List<String>): List<SelectOption> {
        return if (options.size < 2) {
            defaultOptions
        } else IntStream.range(0, options.size)
            .mapToObj { i: Int -> SelectOption.of("%c".formatted('A'.code + i), options[i]) }
            .toList()
    }

    private fun getJoinedOptions(options: List<String>): String {
        return IntStream.range(0, options.size)
            .mapToObj { i: Int -> ":regional_indicator_%c: ❱❱ ".formatted('a'.code + i) + options[i] }
            .collect(Collectors.joining("\n"))
    }

    //
    //    private MongoCollection<Poll> getCollection() {
    //        return MindBot.getDatabase().getCollection("polls", Poll.class);
    //    }
    //
    //    private void createPollEntry(String messageId) {
    //        getCollection().insertOne(new Poll(messageId));
    //    }
    //
    //    private void updatePollEntry(String voteId, String messageId, String selectedOption) {
    //        getCollection().updateOne(eq("vote_id", voteId), set("votes."+messageId, selectedOption));
    //    }
    private fun handlePollEnd(event: SlashCommandInteractionEvent) {
        val id = event.getOption("poll-id")!!.asString
        //        Poll poll = getCollection().findOneAndDelete(eq("vote_id", id));

//        if (poll == null) {
//            event.deferReply(true)
//                 .setContent("Poll not found")
//                 .queue();
//            return;
//        }
//
        val result = StringBuilder().append("Poll results: \n")

//        poll.getVotes()
//            .entrySet()
//            .stream()
//            .collect(
//                Collectors.groupingBy(Map.Entry::getValue, Collectors.counting())
//            )
//            .forEach(
//                (option, count) -> result.append("%s: %d\n".formatted(option, count))
//            );
        //https://gist.github.com/kkrypt0nn/a02506f3712ff2d1c8ca7c9e0aed7c06
        // TODO: separate results into a different method and make it pretty
        event.channel
            .editMessageById(id, "")
            .content(result.toString())
            .setActionRows()
            .setEmbeds()
            .queue()
        event.deferReply(true)
            .setContent("Poll %s successfully ended".formatted(id))
            .queue()
    }
}
