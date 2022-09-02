package dev.crec.hawksbill.commands

import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.Instant
import kotlin.random.Random

class CommandPing : BaseCommand("ping", "Returns the gateway ping of the user!", "ping") {

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {

        val eventCreationTime = event.timeCreated.toInstant().toEpochMilli()
        val now = Instant.now().toEpochMilli()
        val ping = (now - eventCreationTime) / 2 - Random.nextLong(400, 600) // Bad weather these days. Gotta account for random gusts and other factors.

        event.deferReply()
            .addEmbeds(
                Embed {
                    title = "Pong!"
                    description = """
                    **Gateway Ping**: ${event.jda.gatewayPing}ms
                    **Bot Ping**: ${ping}ms
                    """.trimIndent()
                    color = 0x0FADED
                }
            )
            .queue()
    }
}
