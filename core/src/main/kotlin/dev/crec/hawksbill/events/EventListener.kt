package dev.crec.hawksbill.events

import dev.crec.hawksbill.api.HawksBill
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent

class EventListener {
    @SubscribeEvent
    fun onReady(event: ReadyEvent) {
        HawksBill.logger.info("Registering commands for ${event.guildTotalCount} guilds")
        HawksBill.updateCommands()
    }

    @SubscribeEvent
    fun onComponent(event: GenericComponentInteractionCreateEvent) {
        val idArgs = event.componentId.split(":")
        val command = HawksBill.commands[idArgs[0]]!!
        when (event) {
            is ButtonInteractionEvent -> command.onButton(event, idArgs.subList(1, idArgs.size))
            is StringSelectInteractionEvent -> command.onStringSelectMenu(event, idArgs.subList(1, idArgs.size))
            is EntitySelectInteractionEvent -> command.onEntitySelectMenu(event, idArgs.subList(1, idArgs.size))
        }
    }

    @SubscribeEvent
    fun onModal(event: ModalInteractionEvent) {
        val idArgs = event.modalId.split(":")
        val command = HawksBill.commands[idArgs[0]]!!
        command.onModal(event, idArgs.subList(1, idArgs.size))
    }

    @SubscribeEvent
    fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val message = "command for ${event.name} invoked by ${event.user.name}"
        executeSilently("Failed to execute $message") {
            HawksBill.logger.info("Executing $message")
            HawksBill.commands[event.name]!!.onSlashCommand(event)
        }
    }

    @SubscribeEvent
    fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        val message = "autocomplete for ${event.name} invoked by ${event.user.name}"
        executeSilently("Failed to execute $message") {
            HawksBill.logger.info("Executing $message")
            HawksBill.commands[event.name]!!.onAutoComplete(event)
        }
    }

    private inline fun executeSilently(errorMessage: String, invokable: () -> Unit) {
        try {
            invokable.invoke()
        } catch (e: Exception) {
            HawksBill.logger.error(
                """
                        $errorMessage: ${e.message}
                        ${e.stackTraceToString()}
                        """
            )
        }
    }
}
