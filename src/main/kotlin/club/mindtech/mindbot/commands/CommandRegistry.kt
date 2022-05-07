package club.mindtech.mindbot.commands

import net.dv8tion.jda.api.interactions.commands.build.CommandData

private val COMMAND_MAP = setOf(
    CommandArchive(),
    CommandCalc(),
    CommandHelp(),
    CommandPing(),
    CommandPoll(),
).associateBy { it.name }

val registeredCommands = COMMAND_MAP.toMap() // to make a copy

fun getSlashCommandData(): Array<CommandData> {
    return COMMAND_MAP.values.map { it.getCommandData() }.toTypedArray()
}

fun getCommand(command: String?): BaseCommand? {
    return if (command == null) {
        null
    } else {
        COMMAND_MAP[command]
    }
}
