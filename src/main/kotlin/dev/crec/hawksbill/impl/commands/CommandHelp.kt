package dev.crec.hawksbill.impl.commands

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.bot
import dev.crec.hawksbill.utility.extensions.newLine
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.messages.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.utils.MarkdownUtil.bold
import net.dv8tion.jda.api.utils.MarkdownUtil.codeblock

@SlashCommandMarker
class CommandHelp : ICommand {

    override fun commandData(): SlashCommandData {
        return Command(
            "help",
            "Shows usage information about commands"
        ) {
            option<String>(
                name = "command",
                description = "Name of the command to view help for",
                required = true,
                autocomplete = true
            )
        }
    }

    override suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val commandName: String = event.getOption("command")!!.asString.lowercase()
        event.deferReply().addEmbeds(getHelpEmbed(event.user, commandName)).queue()
    }

    override suspend fun onAutoComplete(event: CommandAutoCompleteInteractionEvent) {
        val suggestions = bot.commands.keys
        val userInput = event.focusedOption.value.lowercase()
        event
            .replyChoiceStrings(suggestions.filter { it.contains(userInput) })
            .queue()
    }

    private fun getHelpEmbed(author: User, commandName: String): MessageEmbed {
        val command: ICommand? = bot.commands[commandName]

        val builder = EmbedBuilder {
            footer {
                name = "Requested by ${author.name}"
            }
        }

        if (command == null) {
            builder.title = "Error"
            builder.color = 0xdc2626
            builder.description = codeblock("Unknown command: $commandName")
        } else {
            builder.title = command.name
            builder.color = 0x1dd1a1
            builder.description = getPrettyCommandUsage(command)
        }

        return builder.build()
    }

    private fun getPrettyCommandUsage(command: ICommand): String {
        val cmdName = command.name
        return buildString {

            append(codeblock(command.description))
            newLine()

            val subcommands = command.commandData().subcommands
            if (subcommands.isEmpty())
                return@buildString

            subcommands.forEach { subCmd ->
                append(bold("/$cmdName ${subCmd.name}"))
                append(codeblock(subCmd.description))
                newLine()
            }

            deleteAt(length - 1)
        }
    }
}
