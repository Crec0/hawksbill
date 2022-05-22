package club.mindtech.mindbot.events

import club.mindtech.mindbot.bot
import club.mindtech.mindbot.commands.ID_SEPARATOR
import club.mindtech.mindbot.commands.getCommand
import club.mindtech.mindbot.log
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent

class EventListener {

    @SubscribeEvent
    fun onReady(event: ReadyEvent) {
        bot.registerCommands(event.jda)
        log.info("Commands registered for ${event.guildTotalCount} guild${if (event.guildTotalCount == 1) "" else "s"}!")
    }

    @SubscribeEvent
    fun onComponent(event: GenericComponentInteractionCreateEvent) {
        val idArgs = event.componentId.split(ID_SEPARATOR)
        val command = getCommand(idArgs[0])!!
        when (event) {
            is ButtonInteractionEvent -> command.onButtonInteraction(event, idArgs.subList(1, idArgs.size))
            is SelectMenuInteractionEvent -> command.onMenuInteraction(event, idArgs.subList(1, idArgs.size))
        }
    }

    @SubscribeEvent
    fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val message = "command for ${event.name} invoked by ${event.user.name}"
        executeSafely("Failed to execute $message") {
            log.info("Executing $message")
            getCommand(event.name)!!.onSlashCommand(event)
        }
    }

    @SubscribeEvent
    fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        val message = "autocomplete for ${event.name} invoked by ${event.user.name}"
        executeSafely("Failed to execute $message") {
            log.info("Executing $message")
            getCommand(event.name)!!.onAutoComplete(event)
        }
    }

    private fun executeSafely(errorMessage: String, callable: () -> Unit) {
        try {
            callable.invoke()
        } catch (e: Exception) {
            log.error(
                """
                $errorMessage: ${e.message}
                ${e.stackTraceToString()}
                """
            )
        }
    }
}
