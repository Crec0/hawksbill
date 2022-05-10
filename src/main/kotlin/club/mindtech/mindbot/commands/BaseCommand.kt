package club.mindtech.mindbot.commands

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

const val ID_SEPARATOR = ":"

abstract class BaseCommand(val name: String, val description: String, usage: String) {
    val usage: String

    init {
        this.usage = "/$usage"
    }

    fun generateClassId(vararg ids: String): String = listOf(name, *ids).joinToString(ID_SEPARATOR)

    open fun getCommandData(): SlashCommandData = Commands.slash(name, description)

    open fun onSlashCommand(event: SlashCommandInteractionEvent) {}

    open fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}

    open fun onButtonInteraction(event: ButtonInteractionEvent, idArgs: List<String>) {}

    open fun onMenuInteraction(event: SelectMenuInteractionEvent, idArgs: List<String>) {}
}
