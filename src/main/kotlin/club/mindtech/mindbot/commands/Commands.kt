package club.mindtech.mindbot.commands

import club.mindtech.mindbot.commands.archive.CommandArchive
import club.mindtech.mindbot.commands.help.CommandHelp
import club.mindtech.mindbot.commands.ping.CommandPing
import club.mindtech.mindbot.commands.poll.CommandPoll
import net.dv8tion.jda.api.interactions.commands.build.CommandData

val registeredCommands = setOf(
    CommandPing(),
    CommandHelp(),
    CommandPoll(),
    CommandArchive()
)

fun getSlashCommandData(): Array<CommandData> {
    return registeredCommands.map { it.getCommandData() }.toTypedArray()
}

fun getCommand(command: String?): BaseCommand? {
    return registeredCommands.singleOrNull { it.name == command }
}
