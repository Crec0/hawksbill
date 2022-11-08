package dev.crec.hawksbill.impl.jda

import dev.crec.hawksbill.bot
import dev.crec.hawksbill.mainLogger
import dev.crec.hawksbill.utility.extensions.child
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent

class EventListener: CoroutineEventListener {
    private val log = mainLogger.child("EventListener")

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is CommandAutoCompleteInteractionEvent -> this.onAutoComplete(event)
            is SlashCommandInteractionEvent -> this.onSlashCommand(event)
            is GenericComponentInteractionCreateEvent -> this.onComponent(event)
            is ModalInteractionEvent -> this.onModal(event)
        }
    }

    private suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        log.debug("Executing autocomplete for ${event.name} invoked by ${event.user.name}")
        bot.commands[event.name]!!.onAutoComplete(event)
    }

    private suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        log.debug("Executing command for ${event.name} invoked by ${event.user.name}")
        bot.commands[event.name]!!.onSlashCommand(event)
    }

    private suspend fun onComponent(event: GenericComponentInteractionCreateEvent) {
        val idArgs = event.componentId.split(":")
        val command = bot.commands[idArgs[0]]!!
        when (event) {
            is ButtonInteractionEvent -> command.onButton(event, idArgs.subList(1, idArgs.size))
            is StringSelectInteractionEvent -> command.onStringSelectMenu(event, idArgs.subList(1, idArgs.size))
            is EntitySelectInteractionEvent -> command.onEntitySelectMenu(event, idArgs.subList(1, idArgs.size))
        }
    }

    private suspend fun onModal(event: ModalInteractionEvent) {
        val idArgs = event.modalId.split(":")
        val command = bot.commands[idArgs[0]]!!
        command.onModal(event, idArgs.subList(1, idArgs.size))
    }
}
