package dev.crec.hawksbill.commands

import dev.crec.hawksbill.commands.poll.CommandPoll
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import okhttp3.internal.toImmutableMap

private val COMMAND_MAP = setOf(
    CommandCalc(),
    CommandHelp(),
    CommandPing(),
    CommandPoll(),
    CommandRemindMe(),
).associateBy { it.name }.toImmutableMap()

fun getRegisteredCommands() = COMMAND_MAP

fun getSlashCommandData(): List<CommandData> {
    return COMMAND_MAP.values.map { it.getCommandData() }
}

fun getCommand(command: String?): BaseCommand? {
    return if (command == null) {
        null
    } else {
        COMMAND_MAP[command]
    }
}
