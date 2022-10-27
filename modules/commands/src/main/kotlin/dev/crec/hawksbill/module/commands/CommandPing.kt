package dev.crec.hawksbill.module.commands

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.api.util.alignStringPair
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

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {

        val fields = listOf(
            "Gateway" to event.jda.gatewayPing,
            "Bot" to Random.nextInt(100, 200),
        )
            .map { it.first to it.second.toString() }
            .map { alignStringPair(it, "Gateway".length - it.first.length) }
            .joinToString("") { MarkdownUtil.codeblock(it) }

        event.deferReply()
            .addEmbeds(
                Embed {
                    title = "Pong!"
                    description = fields
                    color = 0x0FADED
                }
            )
            .queue()
    }
}
