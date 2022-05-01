package club.mindtech.mindbot.events

import club.mindtech.mindbot.commands.getCommand
import club.mindtech.mindbot.log
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.hooks.SubscribeEvent

typealias ComponentCallback = (event: GenericComponentInteractionCreateEvent) -> Boolean

private val callbacks: MutableMap<String, ComponentCallback> = HashMap()

fun awaitEvent(id: String, function: ComponentCallback) {
    callbacks[id] = function
}

class InteractionListener {

    @SubscribeEvent
    fun onReady(event: ReadyEvent) {
        log.info("Bot is ready!")
    }

    @SubscribeEvent
    fun onComponent(event: GenericComponentInteractionCreateEvent) {
        val id = event.componentId
        if (callbacks.containsKey(id)) {
            var shouldRemove = true
            executeSafely("Executing component callback for $id") {
                shouldRemove = callbacks[id]!!.invoke(event)
            }
            if (shouldRemove) {
                callbacks.remove(id)
            }
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
                """$errorMessage: ${e.message}
                   ${e.stackTraceToString()}
                """.trimIndent()
            )
        }
    }
}
