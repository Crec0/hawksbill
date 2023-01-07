package dev.crec.hawksbill.impl.commands

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.bot
import dev.crec.hawksbill.impl.database.PollDTO
import dev.crec.hawksbill.utility.Colors
import dev.crec.hawksbill.utility.extensions.EMPTY
import dev.crec.hawksbill.utility.extensions.image
import dev.crec.hawksbill.utility.extensions.rect
import dev.crec.hawksbill.utility.extensions.text
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.SelectOption
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.utils.FileUpload
import org.litote.kmongo.eq
import org.litote.kmongo.keyProjection
import org.litote.kmongo.set
import org.litote.kmongo.setTo
import org.litote.kmongo.unset
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
        isGuildOnly = true

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

        if (bot.config.debugEnabled) {
            subcommand("test", "testing")
        }
    }

    override suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "create" -> handlePollCreate(event)
            "end" -> handlePollEnd(event)
            "test" -> handlePollTest(event)
        }
    }

    private suspend fun handlePollCreate(event: SlashCommandInteractionEvent) {
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

        val message = event
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
            }.await()

        createPollEntry(
            messageId = message.id,
            channelId = message.channel.id,
            question = question,
            options = options
        )
    }

    override suspend fun onButton(event: ButtonInteractionEvent, ids: List<String>) {
        when (ids[0]) {
            PollButtons.VOTE.value -> handleVote(event, ids[1])
            PollButtons.RETRACT.value -> handleRetract(event, ids[1])
        }
    }

    override suspend fun onStringSelectMenu(event: StringSelectInteractionEvent, ids: List<String>) {
        val memberId = event.user.id
        val selected = event.selectedOptions.first().label
        updatePollEntry(ids[0], memberId, selected)
        event.deferEdit().setContent("Your vote has been recorded").setComponents().queue()
    }

    private suspend fun handleVote(event: ButtonInteractionEvent, pollID: String) {
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
                StringSelectMenu.create(selectId)
                    .addOptions(poll.options.map { (k, _) -> SelectOption.of(k, k) })
                    .build()
            )
            .queue()
    }

    private suspend fun handleRetract(event: ButtonInteractionEvent, pollID: String) {
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

    private suspend fun handlePollEnd(event: SlashCommandInteractionEvent) {
        val id = event.getOption("poll-id")!!.asString
        val poll = fetchPoll(id)

        if (poll == null) {
            event.deferReply(true).setContent("Poll not found").queue()
            return
        }

        val channel = event.jda.getTextChannelById(poll.channelId)

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

    private fun handlePollTest(event: SlashCommandInteractionEvent) {
        val poll =
            PollDTO(
                "1",
                "954894993871482950",
                "w",
                getSelectOptions(listOf()),
                mapOf("1" to "Upvote", "4" to "Upvote", "2" to "Downvote")
            )

        event.reply_(
            embeds = listOf(
                Embed {
                    color = 0x06B6D4
                    image = "attachment://$pollImageName"
                }
            ),
            files = listOf(FileUpload.fromData(createPollResultsImage(poll).inputStream(), pollImageName))
        ).queue()
    }

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

    private fun createPollResultsImage(poll: PollDTO): ByteArray {
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

    private suspend fun createPollEntry(
        messageId: String,
        channelId: String,
        question: String,
        options: Map<String, String>
    ) {
        bot.mongoCollection<PollDTO>().insertOne(
            PollDTO(
                voteId = messageId,
                channelId = channelId,
                question = question,
                options = options
            )
        )
    }

    private suspend fun fetchPoll(pollID: String): PollDTO? {
        return bot.mongoCollection<PollDTO>().findOne(PollDTO::voteId eq pollID)
    }

    private suspend fun updatePollEntry(voteId: String, userId: String, selectedOption: String) {
        bot.mongoCollection<PollDTO>().updateOne(
            PollDTO::voteId eq voteId,
            set(PollDTO::votes.keyProjection(key = userId) setTo selectedOption)
        )
    }

    private suspend fun removePollEntry(voteId: String, userId: String) {
        bot.mongoCollection<PollDTO>().updateOne(
            PollDTO::voteId eq voteId,
            unset(PollDTO::votes.keyProjection(key = userId))
        )
    }

    private suspend fun deletePoll(voteId: String) {
        bot.mongoCollection<PollDTO>().deleteOne(PollDTO::voteId eq voteId)
    }
}
