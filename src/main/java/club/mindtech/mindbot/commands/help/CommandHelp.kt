package club.mindtech.mindbot.commands.help

import club.mindtech.mindbot.commands.BaseCommand
import club.mindtech.mindbot.commands.getAllCommands
import club.mindtech.mindbot.commands.getCommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class CommandHelp : BaseCommand("help", "Shows information about other commands", "help [command]") {
    override fun getCommandData(): SlashCommandData {
        return super
            .getCommandData()
            .addOption(
                OptionType.STRING,
                "command",
                "Command name the help should be shown for",
                false,
                true
            )
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val commandName = event.getOption("command")?.asString?.lowercase()
        val embedFor = getCommand(commandName)

        if (embedFor == null) {
            event.replyEmbeds(getUnknownCommandEmbed())
                .queue()
            return
        }

        event.deferReply()
            .addEmbeds(getHelpEmbed(event.user, embedFor))
            .queue()
    }

    override fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        val userInput = event.focusedOption.value
        val commands = getAllCommands()
        val filteredCommands = commands.map { it.name }.filter { it.contains(userInput) }
        event.replyChoiceStrings(filteredCommands)
            .queue()
    }

    companion object {
        private fun getHelpEmbed(author: User, command: BaseCommand): MessageEmbed {
            val embed = EmbedBuilder()
                .setTitle("Command: " + command.name)
                .setDescription(command.description)
                .addField("Usage:", command.usage, true)
                .setColor(0x1dd1a1)
                .setFooter("Requested by " + author.name)
            return embed.build()
        }

        private fun getUnknownCommandEmbed(): MessageEmbed {
            val embed = EmbedBuilder()
                .setTitle("Unknown command")
                .setDescription(
                    """
                    The command you requested is unknown.
                    ${availableCommands()}
                    """
                )
                .setColor(0x1dd1a1)
            return embed.build()
        }

        private fun availableCommands(): String {
            return """
            Available commands:
            ${getAllCommands().joinToString("\n") { "**»** ${it.name}" }}
            """
        }
    }
}
