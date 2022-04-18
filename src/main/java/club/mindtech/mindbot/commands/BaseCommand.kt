package club.mindtech.mindbot.commands

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

abstract class BaseCommand(val name: String, val description: String, usage: String) {
    val usage: String

    init {
        this.usage = "/$usage"
    }

    open fun getCommandData(): SlashCommandData = Commands.slash(name, description)

    open fun onSlashCommand(event: SlashCommandInteractionEvent) {}

    open fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}
}
