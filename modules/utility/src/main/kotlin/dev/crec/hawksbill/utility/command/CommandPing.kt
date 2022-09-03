package dev.crec.hawksbill.utility.command

import dev.crec.hawksbill.api.annotation.Command
import dev.crec.hawksbill.api.command.ICommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import java.time.Instant
import kotlin.random.Random


@Command(
    name = "ping",
    description = "Returns the gateway ping of the user!",
    usage = "ping"
)
class CommandPing : ICommand {

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {

        val eventCreationTime = event.timeCreated.toInstant().toEpochMilli()
        val now = Instant.now().toEpochMilli()

        val ping = (now - eventCreationTime) / 2 - Random.nextLong(400, 600)

        event.deferReply()
            .addEmbeds(
                Embed {
                    title = "Pong!"
                    description =
                        """
                        **Gateway Ping**: ${event.jda.gatewayPing}ms
                        **Bot Ping**: ${ping}ms
                        """.trimIndent()
                    color = 0x0FADED
                }
            )
            .queue()
    }
}
