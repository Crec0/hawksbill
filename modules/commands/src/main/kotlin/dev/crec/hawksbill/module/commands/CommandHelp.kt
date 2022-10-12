package dev.crec.hawksbill.module.commands

import dev.crec.hawksbill.api.HawksBill
import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.messages.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

@SlashCommandMarker
class CommandHelp : ICommand {

    override fun commandData(): SlashCommandData {
        return Command(
            "help",
            "Shows usage information about commands"
        ) {
            option<String>(
                name = "command",
                description = "Command name the help should be shown for",
                autocomplete = true
            )
        }
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val commandName: String? = event.getOption("command")?.asString?.lowercase()
        event.deferReply().addEmbeds(getHelpEmbed(event.user, commandName)).queue()
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        val userInput = event.focusedOption.value
        val matchingCommands = HawksBill.commands.keys.filter { it.contains(userInput) }
        event.replyChoiceStrings(matchingCommands).queue()
    }

    private fun getHelpEmbed(author: User, commandName: String?): MessageEmbed {
        val command: ICommand? = HawksBill.commands[commandName]

        val titleString = if (commandName == null) {
            "All Available commands"
        } else if (command == null) {
            "Command not found"
        } else {
            "Help for command: $command"
        }

        val builder = EmbedBuilder {
            title = titleString
            color = 0x1dd1a1
//            description = command?.description ?: availableCommands()
            footer {
                name = "Requested by ${author.name}"
            }
        }

        command?.let {
            builder.field {
                name = "Usage:"
                value = "command.usage"
                inline = true
            }
        }

        return builder.build()
    }

    private fun availableCommands(): String {
        return """
            Available commands:
//            ${HawksBill.commands.keys.joinToString("\n") { "**»** $it" }}
            """
    }
}
