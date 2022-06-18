package dev.crec.hawksbill.commands.poll

import dev.crec.hawksbill.commands.BaseCommand
import dev.crec.hawksbill.database.Poll
import dev.crec.hawksbill.database.getCollection
import dev.crec.hawksbill.util.EMPTY
import dev.crec.hawksbill.util.zFill
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.keyProjection
import org.litote.kmongo.setTo
import org.litote.kmongo.unset
import org.litote.kmongo.updateOne

private const val IMAGE_NAME = "poll.png"

class CommandPoll : BaseCommand("poll", "Create a poll", "poll <question> [<option>...]") {

    override fun getCommandData(): SlashCommandData {
        return super.getCommandData {
            subcommand("create", "Create a poll") {
                option<String>("question", "The question for the poll", true)
                IntRange(1, 20).map { index ->
                    option<String>("option-${zFill(index, 2)}", "Option $index for the poll")
                }
            }
            subcommand("end", "End a poll") {
                option<String>("poll-id", "The id of the poll to end")
            }
        }
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        when (event.commandPath) {
            "${this.name}/create" -> handlePollCreate(event)
            "${this.name}/end" -> handlePollEnd(event)
        }
    }

    private fun handlePollCreate(event: SlashCommandInteractionEvent) {
        val allOptions = event.options.map { it.asString }.toList()

        val question = allOptions[0]
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
            .queue { message -> createPollEntry(messageId = message.id, question = question, options = options) }
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
        event.deferEdit().setContent("Your vote has been recorded").setActionRows().queue()
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
                SelectMenu.create(selectId).addOptions(poll.options.map { (k, v) -> SelectOption.of(k, v) }).build()
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
        val poll: Poll? = fetchAndDelete(id)

        if (poll == null) {
            event.deferReply(true).setContent("Poll not found").queue()
            return
        }

        event.channel.retrieveMessageById(id).queue { message ->
            val resultEmbed = Embed {
                color = 0x06B6D4
                image = "attachment://$IMAGE_NAME"
            }
            message
                .editMessageEmbeds(*message.embeds.toTypedArray(), resultEmbed)
                .addFile(createPollResultsImage(poll), IMAGE_NAME)
                .flatMap { it.editMessageComponents() }
                .queue()
        }

        event.deferReply(true).setContent("Poll $id successfully ended").queue()
    }

    private fun createPollEntry(messageId: String, question: String, options: Map<String, String>) {
        getCollection<Poll>().insertOne(Poll(vote_id = messageId, question = question, options = options))
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

    private fun fetchAndDelete(voteId: String): Poll? {
        return getCollection<Poll>().findOneAndDelete(Poll::vote_id eq voteId)
    }
}
