package dev.crec.hawksbill.impl.commands

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.interactions.components.ModalBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import kotlin.random.Random

@SlashCommandMarker
class CommandSpinDaWheel : ICommand {
    override fun commandData() = Command(
        name = "spin-da-wheel",
        description = "Rolls a die for you. Default 6 sided die. Sides can be configured."
    ) {
        subcommand(name = "coin", description = "Flip a coin")
        subcommand(name = "die", description = "Roll a die with 6 sides (or arbitrary amount of sides)") {
            option<Int>(name = "sides", description = "Number of sides for the die to roll with.") {
                setRequiredRange(2, 128)
            }
        }
        subcommand(name = "spin", description = "Spin a wheal with user provided options. Options are input in a Modal")
    }

    override suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        when (event.subcommandName) {
            "coin" -> handleCoinFlip(event)
            "die" -> handleDieRoll(event)
            "spin" -> handleSpinWheel(event)
        }
    }

    override suspend fun onModal(event: ModalInteractionEvent, ids: List<String>) {
        val options = event.interaction
            .getValue("options")!!.asString
            .split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        event.reply("Wheel has chosen '${options[Random.nextInt(options.size)]}' for you!").queue()
    }

    private fun handleCoinFlip(event: SlashCommandInteractionEvent) {
        event.reply(listOf("Heads", "Tails")[Random.nextInt(2)]).queue()
    }

    private fun handleDieRoll(event: SlashCommandInteractionEvent) {
        val sides = event.getOption("sides", 6, OptionMapping::getAsInt)
        event.reply("You rolled a ${Random.nextInt(sides)}!").queue()
    }

    private fun handleSpinWheel(event: SlashCommandInteractionEvent) {
        val modal = ModalBuilder(
            id = this.generateComponentId(),
            title = "Add options for spin the wheel!"
        ) {
            paragraph(id = "options", label = "options") {
                title = "Options"
            }
        }.build()

        event.replyModal(modal).queue()
    }
}
