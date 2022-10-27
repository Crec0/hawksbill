package dev.crec.hawksbill.module.commands

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.mariuszgromada.math.mxparser.Expression

@SlashCommandMarker
class CommandCalc : ICommand {

    override fun commandData(): SlashCommandData {
        return Command(
            "calc",
            "Calculates a math expression. For usage, check: https://mathparser.org/mxparser-tutorial"
        ) {
            option<String>(
                name = "expr",
                description = "The expression to evaluate",
                required = true
            )
        }
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val expression = event.getOptionsByName("expression")[0].asString
        val result = Expression(expression.trim()).calculate()

        event.reply("=> $result").queue()
    }
}
