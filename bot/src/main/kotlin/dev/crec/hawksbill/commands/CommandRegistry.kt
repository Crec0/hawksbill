package dev.crec.hawksbill.commands

import dev.crec.hawksbill.commands.poll.CommandPoll

private val COMMAND_MAP = setOf(
    CommandCalc(),
    CommandHelp(),
    CommandPing(),
    CommandPoll(),
    CommandRemindMe(),
).associateBy { it.name }

fun getRegisteredCommands() = COMMAND_MAP

fun getSlashCommandData() = COMMAND_MAP.values.map { it.getCommandData() }

fun getCommand(command: String?) = if (command == null) null else COMMAND_MAP[command]
