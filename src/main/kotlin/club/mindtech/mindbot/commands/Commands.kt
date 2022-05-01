package club.mindtech.mindbot.commands

import club.mindtech.mindbot.commands.archive.CommandArchive
import club.mindtech.mindbot.commands.help.CommandHelp
import club.mindtech.mindbot.commands.ping.CommandPing
import club.mindtech.mindbot.commands.poll.CommandPoll
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import java.util.*

private val COMMANDS: MutableMap<String, BaseCommand> = TreeMap()

private fun register(command: BaseCommand) {
    COMMANDS[command.name] = command
}

private fun registerCommands() {
    register(CommandPing())
    register(CommandPoll())
    register(CommandHelp())
    register(CommandArchive())
}

fun getSlashCommandData(): Array<CommandData> {
    registerCommands()
    return COMMANDS.values.map { obj: BaseCommand -> obj.getCommandData() }.toTypedArray()
}

fun getAllCommands(): List<BaseCommand> {
    return COMMANDS.values.toList()
}

fun getCommand(command: String?): BaseCommand? {
    return if (command == null) null else COMMANDS[command]
}
