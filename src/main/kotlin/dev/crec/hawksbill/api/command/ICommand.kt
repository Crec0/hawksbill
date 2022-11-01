package dev.crec.hawksbill.api.command

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

interface ICommand {

    val name: String
        get() = this.commandData().name

    val description: String
        get() = this.commandData().description


    fun commandData(): SlashCommandData

    // Command Events
    suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {}
    suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {}

    // Component Events
    suspend fun onButton(event: ButtonInteractionEvent, ids: List<String>) {}
    suspend fun onModal(event: ModalInteractionEvent, ids: List<String>) {}
    suspend fun onStringSelectMenu(event: StringSelectInteractionEvent, ids: List<String>) {}
    suspend fun onEntitySelectMenu(event: EntitySelectInteractionEvent, ids: List<String>) {}

    // Utils
    fun generateComponentId(vararg parts: String) = listOf(this.name, *parts).joinToString(":")
}
