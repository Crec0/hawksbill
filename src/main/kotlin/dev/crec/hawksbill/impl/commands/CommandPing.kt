package dev.crec.hawksbill.impl.commands

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.bot
import dev.crec.hawksbill.utility.extensions.alignStringPair
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.MarkdownUtil
import kotlin.random.Random


@SlashCommandMarker
class CommandPing : ICommand {
    override fun commandData() = Command(
        "ping",
        "Returns the gateway ping of the user."
    )

    override suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val isEasterEggPing = Random.nextInt(20) == 0

        val embed = if (isEasterEggPing) {
            Embed {
                title = "Pong!"
                image = bot.tenorAPI.getRandomGifUrl("discord ping")
                color = 0x0FADED
            }
        } else {
            val fields = listOf(
                "Gateway" to event.jda.gatewayPing,
                "Bot" to Random.nextInt(100, 200),
            )
            val maxLength = fields.maxOfOrNull { it.first }!!.length
            val body = fields
                .map { it.first to it.second.toString() }
                .map { alignStringPair(it, maxLength - it.first.length) }
                .joinToString("") { MarkdownUtil.codeblock(it) }

            Embed {
                title = "Pong!"
                description = body
                color = 0x0FADED
            }
        }

        event.deferReply()
            .addEmbeds(embed)
            .queue()
    }
}
