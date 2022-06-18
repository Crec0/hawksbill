package dev.crec.hawksbill.commands

import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.messages.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class CommandHelp : BaseCommand("help", "Shows information about other commands", "help [command]") {

    override fun getCommandData(): SlashCommandData {
        return super.getCommandData {
            option<String>(
                name = "command",
                description = "Command name the help should be shown for",
                required = false,
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
        val matchingCommands = getRegisteredCommands().keys.filter { it.contains(userInput) }
        event.replyChoiceStrings(matchingCommands).queue()
    }

    private fun getHelpEmbed(author: User, commandName: String?): MessageEmbed {
        val command: BaseCommand? = getCommand(commandName)

        val titleString = if (commandName == null) {
            "All Available commands"
        } else if (command == null) {
            "Command not found"
        } else {
            "Help for command: ${command.name}"
        }

        val builder = EmbedBuilder {
            title = titleString
            color = 0x1dd1a1
            description = command?.description ?: availableCommands()
            footer {
                name = "Requested by ${author.name}"
            }
        }

        command?.let {
            builder.field {
                name = "Usage:"
                value = command.usage
                inline = true
            }
        }

        return builder.build()
    }

    private fun availableCommands(): String {
        return """
            Available commands:
            ${getRegisteredCommands().keys.joinToString("\n") { "**»** $it" }}
            """
    }
}
