package dev.crec.hawksbill.commands

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData


abstract class BaseCommand(val name: String, val description: String, usage: String) {
    val usage: String

    init {
        this.usage = "/$usage"
    }

    fun generateClassId(vararg ids: String): String = listOf(name, *ids).joinToString(":")

    open fun getCommandData(): SlashCommandData = Commands.slash(name, description)

    fun getCommandData(block: SlashCommandData.() -> Unit): SlashCommandData {
        val data = Commands.slash(name, description)
        data.block()
        return data
    }

    open fun onSlashCommand(event: SlashCommandInteractionEvent) {}

    open fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}

    open fun onButtonInteraction(event: ButtonInteractionEvent, idArgs: List<String>) {}

    open fun onMenuInteraction(event: SelectMenuInteractionEvent, idArgs: List<String>) {}
}
