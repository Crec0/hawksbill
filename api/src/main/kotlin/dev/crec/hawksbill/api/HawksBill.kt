package dev.crec.hawksbill.api

import com.mongodb.client.MongoDatabase
import dev.crec.hawksbill.api.command.ICommand
import net.dv8tion.jda.api.JDA
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object HawksBill {
    val logger = LoggerFactory.getLogger(HawksBill::class.java) as Logger
    val commands: Map<String, ICommand> = mutableMapOf()

    lateinit var database: MongoDatabase
    lateinit var jda: JDA

    fun registerCommands(commandsSet: Set<ICommand>) {
        commandsSet.forEach { cmd ->
            (commands as MutableMap<String, ICommand>)[cmd.name] = cmd
        }
    }

    fun updateCommands() {
        val commandData = commands.values.map { cmd -> cmd.commandData() }
        jda.guilds.forEach { guild ->
            guild.updateCommands().apply {
                addCommands(commandData)
            }.queue()
        }
        logger.info("Registered ${commandData.size} commands for ${jda.guilds.size} guild(s)")
    }
}
