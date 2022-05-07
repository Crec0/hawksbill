package club.mindtech.mindbot.commands

import dev.minn.jda.ktx.interactions.commands.option
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.mariuszgromada.math.mxparser.Expression

class CommandCalc :
    BaseCommand(
        name = "calc",
        description = "Calculates a math expression. For usage, check: https://mathparser.org/mxparser-tutorial",
        usage = "calc <expression>"
    ) {

    override fun getCommandData(): SlashCommandData {
        return super
            .getCommandData()
            .option<String>(
                name = "expression",
                description = "The expression to calculate",
                required = true,
                autocomplete = false
            )
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val expression = event.getOptionsByName("expression")[0].asString
        val result = Expression(expression.trim()).calculate()

        event.reply("=> $result").queue()
    }
}
