package club.mindtech.mindbot.commands

import net.dv8tion.jda.api.interactions.commands.build.CommandData
import okhttp3.internal.toImmutableMap

private val COMMAND_MAP = setOf(
    CommandArchive(),
    CommandCalc(),
    CommandHelp(),
    CommandPing(),
    CommandPoll(),
).associateBy { it.name }.toImmutableMap()

fun getRegisteredCommands() = COMMAND_MAP

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
