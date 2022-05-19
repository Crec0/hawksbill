package club.mindtech.mindbot.commands

import club.mindtech.mindbot.bot
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class CommandPing : BaseCommand("ping", "Returns the gateway ping of the user!", "ping") {
    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        bot.jda.restPing.queue { event.deferReply().setContent("Pong! $it ms").queue() }
    }
}
