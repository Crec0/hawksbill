package dev.crec.hawksbill.commands.poll

import dev.crec.hawksbill.database.Poll
import dev.crec.hawksbill.helpers.Colors
import dev.crec.hawksbill.helpers.image
import dev.crec.hawksbill.helpers.ptToPx
import dev.crec.hawksbill.helpers.rect
import dev.crec.hawksbill.helpers.text
import dev.crec.hawksbill.log
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

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

fun createPollResultsImage(poll: Poll): ByteArray {
    val spacing = 8
    val fontSize = 18
    val imgWidth = 512

    val rectSliceSize = 3

    val imgHeight = fontSize + spacing * 2 + (poll.options.size * (fontSize + spacing))

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
            fontName = "Montserrat SemiBold",
            color = Colors.BLUE_GRAY_600
        )

        val maxWidthLabel = poll.options.keys.maxByOrNull { it.length } ?: ""
        val maxLabelWidth = fontMetrics.stringWidth(maxWidthLabel).ptToPx()

        poll.options.keys.forEachIndexed { index, label ->

            val votes = groupedVotes[label] ?: 0
            val percentage = (votes * 100) / totalVotes
            val rectWidth = percentage * rectSliceSize

            val textY = (fontSize + spacing) * (2 + index)
            val textWidth = fontMetrics.stringWidth(label).ptToPx()

            text(
                x = maxLabelWidth - textWidth + spacing,
                y = textY,
                text = label,
                fontName = "Montserrat SemiBold",
                color = Colors.BLUE_GRAY_600
            )

            rect(
                x = maxLabelWidth + textWidth + spacing * 2,
                y = textY - fontSize,
                width = rectWidth,
                height = fontSize + 4,
                color = rankingColor(rankings[label]!!),
                fill = true
            )

            text(
                x = maxLabelWidth + textWidth + spacing * 3 + rectWidth,
                y = textY,
                text = "$votes [ $percentage % ]",
                fontName = "Montserrat SemiBold",
                color = Colors.BLUE_GRAY_600
            )
        }
    }
    val buffer = ByteArrayOutputStream()
    ImageIO.write(image, "png", buffer)

    return buffer.toByteArray()
}
