package club.mindtech.mindbot.commands.help

import club.mindtech.mindbot.commands.BaseCommand
import club.mindtech.mindbot.commands.getCommand
import club.mindtech.mindbot.commands.registeredCommands
import dev.minn.jda.ktx.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class CommandHelp :
    BaseCommand("help", "Shows information about other commands", "help [command]") {
    override fun getCommandData(): SlashCommandData {
        return super.getCommandData()
            .addOption(
                OptionType.STRING, "command", "Command name the help should be shown for", false, true
            )
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val commandName: String? = event.getOption("command")?.asString?.lowercase()
        event.deferReply().addEmbeds(getHelpEmbed(event.user, commandName)).queue()
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        val userInput = event.focusedOption.value
        val matchingCommands = registeredCommands.map { it.name }.filter { it.contains(userInput) }
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
            ${registeredCommands.joinToString("\n") { "**»** ${it.name}" }}
            """
    }
}
