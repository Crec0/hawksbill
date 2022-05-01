package club.mindtech.mindbot.commands.poll

import club.mindtech.mindbot.MindBot
import club.mindtech.mindbot.commands.BaseCommand
import club.mindtech.mindbot.database.Poll
import club.mindtech.mindbot.util.button
import club.mindtech.mindbot.util.menu
import club.mindtech.mindbot.util.zFill
import com.mongodb.client.MongoCollection
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.subcommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import org.litote.kmongo.* // ktlint-disable no-wildcard-imports
import kotlin.math.ceil

class CommandPoll : BaseCommand("poll", "Create a poll", "poll <question> [<option>...]") {
    override fun getCommandData(): SlashCommandData {

        val options =
            IntRange(1, 20)
                .map {
                    OptionData(OptionType.STRING, "option-${zFill(it, 2)}", "Option $it for the poll")
                }
                .toTypedArray()

        return super.getCommandData()
            .subcommand("create", "Create a poll") {
                option<String>("question", "The question for the poll", true)
                    .addOptions(*options)
            }
            .subcommand("end", "End a poll") {
                option<String>("poll-id", "The id of the poll to end")
            }
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        when (event.commandPath) {
            "poll/create" -> handlePollCreate(event)
            "poll/end" -> handlePollEnd(event)
        }
    }

    private fun handlePollCreate(event: SlashCommandInteractionEvent) {
        val allOptions = event.options.map { it.asString }.toList()

        val question = allOptions[0]
        val options = allOptions.subList(1, allOptions.size).toSet().toList()

        event
            .deferReply()
            .flatMap { it.editOriginal(EmbedBuilder.ZERO_WIDTH_SPACE) }
            .flatMap {
                val id = it.id
                val embed = Embed {
                    title = question
                    color = 0x06B6D4
                    description = getJoinedOptions(options)
                    footer { name = "Poll ID: $id" }
                }
                return@flatMap it
                    .editMessageEmbeds(embed)
                    .setActionRow(
                        getVoteButton(id, question, options),
                        getRetractButton(id)
                    )
            }
            .queue { createPollEntry(it.id) }
    }

    private fun getVoteButton(id: String, question: String, options: List<String>): Button {
        return button(ButtonStyle.PRIMARY, "Vote") {
            it.deferReply(true)
                .setContent("Vote for a $question")
                .addActionRow(getSelectMenu(id, options))
                .queue()
            return@button false
        }
    }

    private fun getRetractButton(pollID: String): Button {
        return button(ButtonStyle.DANGER, "Retract Vote") {
            it.deferReply(true).setContent("Vote removed").queue()
            removePollEntry(pollID, it.user.id)
            return@button false
        }
    }

    private fun getSelectMenu(id: String, options: List<String>): SelectMenu {
        return menu(getSelectOptions(options)) {
            if (it is SelectMenuInteractionEvent) {
                val memberId = it.user.id
                val selected = it.selectedOptions.first().value
                updatePollEntry(id, memberId, selected)
                it.deferEdit().setContent("Your vote has been recorded").setActionRows().queue()
            }
            return@menu true
        }
    }

    private val defaultOptions: List<SelectOption> =
        listOf(SelectOption.of("Upvote", "upvote"), SelectOption.of("Downvote", "downvote"))

    private fun getSelectOptions(options: List<String>): List<SelectOption> {
        return if (options.size < 2) {
            defaultOptions
        } else {
            IntRange(0, options.size - 1)
                .map {
                    val char = ('A'.code + it).toChar()
                    SelectOption.of("$char", "$char.${options[it]}")
                }
                .toList()
        }
    }

    private fun getJoinedOptions(options: List<String>): String {
        return IntRange(0, options.size - 1).joinToString("\n") {
            ":regional_indicator_${('a'.code + it).toChar()}: ❱❱ ${options[it]}"
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
                title = "Poll Results"
                color = 0x06B6D4
                description = getFormattedResults(poll.votes)
            }
            message
                .editMessageEmbeds(*message.embeds.toTypedArray(), resultEmbed)
                .flatMap { it.editMessageComponents() }
                .queue()
        }

        event.deferReply(true).setContent("Poll $id successfully ended").queue()
    }

    private fun getFormattedResults(votes: Map<String, String>): String {
        val totalVotes = votes.size + 1
        val voteFrequencies = votes.entries.groupBy { it.value }.mapValues { it.value.size }
        val histogram = voteFrequencies.entries.map { (option, count) ->
            val segments = ceil(count.toFloat() / totalVotes.toFloat()).toInt()
            val bar = "█".repeat(segments)
            "$option: $bar $count"
        }

        println(voteFrequencies)
        println(histogram)

        return """
            |```
            |Total votes: ${totalVotes - 1}
            |
            |${histogram.joinToString("\n")}
            |```
        """.trimMargin()
    }

    private fun getCollection(): MongoCollection<Poll> {
        return MindBot.db.getCollection()
    }

    private fun createPollEntry(messageId: String) {
        getCollection().insertOne(Poll(messageId))
    }

    private fun updatePollEntry(voteId: String, userId: String, selectedOption: String) {
        getCollection()
            .updateOne(
                Poll::vote_id eq voteId,
                Poll::votes.keyProjection(userId) setTo selectedOption
            )
    }

    private fun removePollEntry(voteId: String, userId: String) {
        getCollection().updateOne(Poll::vote_id eq voteId, unset(Poll::votes.keyProjection(userId)))
    }

    private fun fetchAndDelete(voteId: String): Poll? {
        return getCollection().findOneAndDelete(Poll::vote_id eq voteId)
    }
}
