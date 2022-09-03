package dev.crec.hawksbill.events

import dev.crec.hawksbill.bot
import dev.crec.hawksbill.commands.getCommand
import dev.crec.hawksbill.log
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
        val idArgs = event.componentId.split(":")
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
//
//    @SubscribeEvent
//    fun onMessage(event: MessageReceivedEvent) {
//        if (event.author.isBot) return
//
//        if (event.channel.id == env("CHAT_BRIDGE_CHANNEL")) {
//            val message = event.message.contentDisplay
//            runBlocking {
//                rconInstance().chatMessage("[${event.member!!.effectiveName}] $message")
//            }
//        }
//    }

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
