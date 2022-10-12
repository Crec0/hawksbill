package dev.crec.hawksbill.api.command

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface ICommand {

    val name: String
        get() = this.commandData().name

    val description: String
        get() = this.commandData().description


    fun commandData(): SlashCommandData

    // Events
    fun onSlashCommand(event: SlashCommandInteractionEvent) {}
    fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}
    fun onButtonInteraction(event: ButtonInteractionEvent, ids: List<String>) {}
    fun onMenuInteraction(event: SelectMenuInteractionEvent, ids: List<String>) {}

    // Utils
    fun generateComponentId(vararg parts: String) = listOf(this.name, *parts).joinToString(":")
}
