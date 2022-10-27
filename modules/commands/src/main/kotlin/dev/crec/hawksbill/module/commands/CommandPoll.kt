package dev.crec.hawksbill.module.commands

import dev.crec.hawksbill.api.HawksBill
import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.api.util.Colors
import dev.crec.hawksbill.api.util.EMPTY
import dev.crec.hawksbill.api.util.image
import dev.crec.hawksbill.api.util.rect
import dev.crec.hawksbill.api.util.text
import dev.crec.hawksbill.module.database.Poll
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.utils.FileUpload
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.keyProjection
import org.litote.kmongo.setTo
import org.litote.kmongo.unset
import org.litote.kmongo.updateOne
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.max

@SlashCommandMarker
class CommandPoll : ICommand {

    private val pollImageName = "poll.png"

    override fun commandData() = Command(
        "poll",
        "Create and manage polls"
    ) {
        subcommand(name = "create", description = "Create a poll") {
            option<String>(
                name = "question",
                description = "The question for the poll",
                required = true
            )
            IntRange(1, 20).map { index ->
                option<String>(
                    "option-${index.toString().padStart(2, '0')}",
                    "Option $index for the poll"
                )
            }
        }

        subcommand(name = "end", description = "End a poll") {
            option<String>(
                name = "poll-id",
                description = "The id of the poll to end",
                required = true
            )
        }

//            subcommand("test", "testing")
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

                val voteButtonId = generateComponentId(PollButtons.VOTE.value, pollId)
                val retractVoteId = generateComponentId(PollButtons.RETRACT.value, pollId)

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

    override fun onButtonInteraction(event: ButtonInteractionEvent, ids: List<String>) {
        when (ids[0]) {
            PollButtons.VOTE.value -> handleVote(event, ids[1])
            PollButtons.RETRACT.value -> handleRetract(event, ids[1])
        }
    }

    override fun onMenuInteraction(event: SelectMenuInteractionEvent, ids: List<String>) {
        val memberId = event.user.id
        val selected = event.selectedOptions.first().label
        updatePollEntry(ids[0], memberId, selected)
        event.deferEdit().setContent("Your vote has been recorded").setComponents().queue()
    }

    private fun handleVote(event: ButtonInteractionEvent, pollID: String) {
        val deferredReply = event.deferReply(true)
        val poll = fetchPoll(pollID)

        if (poll == null) {
            deferredReply.setContent("Poll not found").queue()
            return
        }

        val selectId = generateComponentId(pollID)

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
        val id = event.getOption("poll-id")!!.asString
        val poll = fetchPoll(id)

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
                image = "attachment://$pollImageName"
            }
            message
                .editMessageEmbeds(*message.embeds.toTypedArray(), resultEmbed)
                .setFiles(FileUpload.fromData(createPollResultsImage(poll), pollImageName))
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

    enum class PollButtons(val value: String) {
        VOTE("Vote"),
        RETRACT("Retract"),
    }

    private fun rankingColor(rank: Int): Colors {

        return when (rank) {
            1 -> Colors.LIGHT_BLUE_400
            2 -> Colors.EMERALD_400
            3 -> Colors.AMBER_400
            else -> Colors.VIOLET_200
        }
    }

    private fun sortVotesToRanks(ranks: List<Pair<String, Int>>): Map<String, Int> {
        var lastVotes = -1
        var lastRank = 0
        val tempMap = mutableMapOf<String, Int>()

        ranks.forEach { (name, votes) ->
            if (votes != lastVotes) {
                lastRank++
            }
            tempMap[name] = lastRank
            lastVotes = votes
        }

        return tempMap
    }

    private fun createPollResultsImage(poll: Poll): ByteArray {
        val spacing = 10
        val fontSize = 20
        val imgWidth = 512
        val fontName = "Montserrat SemiBold"

        val rectSliceSize = 3

        val imgHeight = fontSize + spacing * 3 + (poll.options.size * (fontSize + spacing))

        val groupedVotes = poll
            .votes
            .entries
            .groupBy { it.value }
            .mapValues { it.value.size }

        val totalVotes = groupedVotes.values.sum().let { if (it == 0) 1 else it }

        val sortedVoterVotePairs = poll
            .options
            .keys
            .map { it to (groupedVotes[it] ?: 0) }
            .sortedByDescending { it.second }

        val rankings = sortVotesToRanks(sortedVoterVotePairs)

        val image = image(imgWidth, imgHeight) {
            rect(x = 0, y = 0, width = imgWidth, height = imgHeight, color = Colors.GRAY_100, fill = true)

            text(
                x = spacing,
                y = spacing + fontSize,
                text = "Poll Results",
                fontName = fontName,
                color = Colors.BLUE_GRAY_600
            )

            val maxWidthLabel = poll.options.keys.maxByOrNull { it.length } ?: ""
            val maxLabelWidth = max(fontMetrics.stringWidth(maxWidthLabel), 40)

            poll.options.keys.forEachIndexed { index, label ->

                val votes = groupedVotes[label] ?: 0
                val percentage = (votes * 100) / totalVotes
                val rectWidth = percentage * rectSliceSize

                val textY = (fontSize + spacing) * (2 + index)
                val textWidth = fontMetrics.stringWidth(label)

                text(
                    x = maxLabelWidth - textWidth + spacing,
                    y = textY,
                    text = label,
                    fontName = fontName,
                    color = Colors.BLUE_GRAY_600
                )

                rect(
                    x = maxLabelWidth + spacing * 2,
                    y = textY - fontSize,
                    width = rectWidth,
                    height = fontSize + 4,
                    color = rankingColor(rankings[label]!!),
                    fill = true
                )

                text(
                    x = maxLabelWidth + spacing * 3 + rectWidth,
                    y = textY,
                    text = "$votes [ $percentage % ]",
                    fontName = fontName,
                    color = Colors.BLUE_GRAY_600
                )
            }
        }
        val buffer = ByteArrayOutputStream()
        ImageIO.write(image, "png", buffer)

        return buffer.toByteArray()
    }

    private fun createPollEntry(messageId: String, channelId: String, question: String, options: Map<String, String>) {
        HawksBill.database.getCollection<Poll>().insertOne(
            Poll(
                vote_id = messageId,
                channel_id = channelId,
                question = question,
                options = options
            )
        )
    }

    private fun fetchPoll(pollID: String): Poll? {
        return HawksBill.database.getCollection<Poll>().findOne(Poll::vote_id eq pollID)
    }

    private fun updatePollEntry(voteId: String, userId: String, selectedOption: String) {
        HawksBill.database.getCollection<Poll>()
            .updateOne(
                Poll::vote_id eq voteId,
                Poll::votes.keyProjection(key = userId) setTo selectedOption
            )
    }

    private fun removePollEntry(voteId: String, userId: String) {
        HawksBill.database.getCollection<Poll>()
            .updateOne(
                Poll::vote_id eq voteId,
                unset(Poll::votes.keyProjection(userId))
            )
    }

    private fun deletePoll(voteId: String) {
        HawksBill.database.getCollection<Poll>().deleteOne(Poll::vote_id eq voteId)
    }
}
