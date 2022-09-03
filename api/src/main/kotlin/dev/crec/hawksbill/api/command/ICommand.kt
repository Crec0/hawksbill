package dev.crec.hawksbill.api.command

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent

interface ICommand {
    fun onSlashCommand(event: SlashCommandInteractionEvent) {}
    fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}
    fun onButtonInteraction(event: ButtonInteractionEvent, ids: List<String>) {}
    fun onMenuInteraction(event: SelectMenuInteractionEvent, ids: List<String>) {}

    fun generateComponentId(vararg parts: String) = listOf(*parts).joinToString(":")
}
