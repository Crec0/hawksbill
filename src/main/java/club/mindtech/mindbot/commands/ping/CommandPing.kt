package club.mindtech.mindbot.commands.ping

import club.mindtech.mindbot.MindBot
import club.mindtech.mindbot.commands.BaseCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class CommandPing : BaseCommand("ping", "Returns the gateway ping of the user!", "ping") {
    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        MindBot.jda.restPing.queue {
            event.deferReply().setContent("Pong! $it ms").queue()
        }
    }
}
