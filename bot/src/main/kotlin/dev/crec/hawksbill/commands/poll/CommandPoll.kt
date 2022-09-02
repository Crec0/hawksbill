package dev.crec.hawksbill.commands.poll

import dev.crec.hawksbill.commands.BaseCommand
import dev.crec.hawksbill.database.Poll
import dev.crec.hawksbill.database.getCollection
import dev.crec.hawksbill.util.EMPTY
import dev.crec.hawksbill.util.zFill
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.utils.FileUpload
import org.litote.kmongo.*

private const val IMAGE_NAME = "poll.png"

class CommandPoll : BaseCommand("poll", "Create a poll", "poll <question> [<option>...]") {

    override fun getCommandData(): SlashCommandData {
        return super.getCommandData {
            subcommand(name = "create", description = "Create a poll") {
                option<String>(name = "question", description = "The question for the poll", required = true)
                IntRange(1, 20).map { index ->
                    option<String>("option-${zFill(index, 2)}", "Option $index for the poll")
                }
            }
            subcommand(name = "end", description = "End a poll") {
                option<String>(name = "poll-id", description = "The id of the poll to end", required = true)
            }
//            subcommand("test", "testing")
        }
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        when (event.commandPath) {
            "${this.name}/create" -> handlePollCreate(event)
            "${this.name}/end" -> handlePollEnd(event)
//            "${this.name}/test" -> handlePollTest(event)
        }
    }

    private fun handlePollCreate(event: SlashCommandInteractionEvent) {
        val allOptions = event.options.map { it.asString }.toList()

        val question = allOptions[0]

        if (question.length > MessageEmbed.TITLE_MAX_LENGTH) {
            event
                .deferReply(true)
                .setContent("Question must be less than 256 characters")
                .queue()
            return
        }

        val distinctOptions = allOptions.subList(1, allOptions.size).distinct()
        val options = getSelectOptions(distinctOptions)

        event
            .deferReply()
            .flatMap { it.editOriginal(EMPTY) }
            .flatMap { message ->
                val pollId = message.id

                val embed = Embed {
                    title = question
                    color = 0x06B6D4
                    description = getJoinedOptions(distinctOptions)
                    footer { name = "Poll ID: $pollId" }
                }

                val voteButtonId = generateClassId(PollButtons.VOTE.value, pollId)
                val retractVoteId = generateClassId(PollButtons.RETRACT.value, pollId)

                return@flatMap message
                    .editMessageEmbeds(embed)
                    .setActionRow(
                        Button.primary(voteButtonId, PollButtons.VOTE.value),
                        Button.danger(retractVoteId, PollButtons.RETRACT.value)
                    )
            }
            .queue { message ->
                createPollEntry(
                    messageId = message.id,
                    channelId = message.channel.id,
                    question = question,
                    options = options
                )
            }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent, idArgs: List<String>) {
        when (idArgs[0]) {
            PollButtons.VOTE.value -> handleVote(event, idArgs[1])
            PollButtons.RETRACT.value -> handleRetract(event, idArgs[1])
        }
    }

    override fun onMenuInteraction(event: SelectMenuInteractionEvent, idArgs: List<String>) {
        val memberId = event.user.id
        val selected = event.selectedOptions.first().label
        updatePollEntry(idArgs[0], memberId, selected)
        event.deferEdit().setContent("Your vote has been recorded").setComponents().queue()
    }

    private fun handleVote(event: ButtonInteractionEvent, pollID: String) {
        val deferredReply = event.deferReply(true)
        val poll = fetchPoll(pollID)

        if (poll == null) {
            deferredReply.setContent("Poll not found").queue()
            return
        }

        val selectId = generateClassId(pollID)

        deferredReply
            .setContent("Vote for ${poll.question}")
            .addActionRow(
                SelectMenu.create(selectId).addOptions(poll.options.map { (k, _) -> SelectOption.of(k, k) }).build()
            )
            .queue()
    }

    private fun handleRetract(event: ButtonInteractionEvent, pollID: String) {
        event.deferReply(true).setContent("Vote removed").queue()
        removePollEntry(pollID, event.user.id)
    }

    private fun getSelectOptions(options: List<String>): Map<String, String> {
        return if (options.size < 2) {
            mapOf(
                "Upvote" to "upvote",
                "Downvote" to "downvote"
            )
        } else {
            IntRange(0, options.size - 1).associate {
                val char = ('A'.code + it).toChar()
                "$char" to options[it]
            }
        }
    }

    private fun getJoinedOptions(options: List<String>): String {
        return IntRange(0, options.size - 1)
            .joinToString("\n") {
                ":regional_indicator_${('a'.code + it).toChar()}: ${"**❱**"} ${options[it]}"
            }
    }

    private fun handlePollEnd(event: SlashCommandInteractionEvent) {
        val id: String = event.getOption("poll-id")!!.asString
        val poll: Poll? = fetchPoll(id)

        if (poll == null) {
            event.deferReply(true).setContent("Poll not found").queue()
            return
        }

        val channel = event.jda.getTextChannelById(poll.channel_id)

        if (channel == null) {
            event.deferReply(true).setContent("Poll channel not found").queue()
            return
        }

        channel.retrieveMessageById(id).queue { message ->
            val resultEmbed = Embed {
                color = 0x06B6D4
                image = "attachment://$IMAGE_NAME"
            }
            message
                .editMessageEmbeds(*message.embeds.toTypedArray(), resultEmbed)
                .setFiles(FileUpload.fromData(createPollResultsImage(poll), IMAGE_NAME))
                .flatMap { it.editMessageComponents() }
                .queue()
        }
        deletePoll(id)
        event.deferReply(true).setContent("Poll $id successfully ended").queue()
    }

//    private fun handlePollTest(event: SlashCommandInteractionEvent) {
//        val poll =
//            Poll("1", "954894993871482950", "w", getSelectOptions(listOf()), mapOf("1" to "Upvote", "4" to "Upvote", "2" to "Downvote"))
//
//        event.reply_(
//            embed = Embed {
//                color = 0x06B6D4
//                image = "attachment://$IMAGE_NAME"
//            },
//            file = NamedFile(IMAGE_NAME, createPollResultsImage(poll).inputStream())
//        ).queue()
//    }

    private fun createPollEntry(messageId: String, channelId: String, question: String, options: Map<String, String>) {
        getCollection<Poll>().insertOne(
            Poll(
                vote_id = messageId,
                channel_id = channelId,
                question = question,
                options = options
            )
        )
    }

    private fun fetchPoll(pollID: String): Poll? {
        return getCollection<Poll>().findOne(Poll::vote_id eq pollID)
    }

    private fun updatePollEntry(voteId: String, userId: String, selectedOption: String) {
        getCollection<Poll>()
            .updateOne(
                Poll::vote_id eq voteId,
                Poll::votes.keyProjection(key = userId) setTo selectedOption
            )
    }

    private fun removePollEntry(voteId: String, userId: String) {
        getCollection<Poll>()
            .updateOne(
                Poll::vote_id eq voteId,
                unset(Poll::votes.keyProjection(userId))
            )
    }

    private fun deletePoll(voteId: String) {
        getCollection<Poll>().deleteOne(Poll::vote_id eq voteId)
    }
}
