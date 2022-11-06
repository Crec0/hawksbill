package dev.crec.hawksbill.impl.commands

import dev.crec.hawksbill.api.annotation.SlashCommandMarker
import dev.crec.hawksbill.api.command.ICommand
import dev.crec.hawksbill.bot
import dev.crec.hawksbill.impl.database.ReminderDTO
import dev.crec.hawksbill.utility.Colors
import dev.crec.hawksbill.utility.extensions.SPACE_1EM
import dev.crec.hawksbill.utility.extensions.hashString
import dev.crec.hawksbill.utility.extensions.truncate
import dev.minn.jda.ktx.interactions.commands.Command
import dev.minn.jda.ktx.interactions.commands.option
import dev.minn.jda.ktx.interactions.commands.subcommand
import dev.minn.jda.ktx.messages.Embed
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import org.litote.kmongo.eq
import java.lang.Integer.min
import java.time.Instant
import kotlin.time.Duration

@SlashCommandMarker
class CommandRemindMe : ICommand {

    override fun commandData(): SlashCommandData {
        return Command(
            "remindme",
            "Create and manage reminders"
        ) {
            subcommand("add", "Adds a reminder") {
                option<String>(
                    name = "time",
                    description = "The time to remind you of",
                    required = true
                )
                option<String>(
                    name = "message",
                    description = "The message to remind you of",
                    required = true
                )
            }
            subcommand("cancel", "Cancels a reminder") {
                option<String>(
                    name = "id",
                    description = "The id of the reminder to cancel",
                    required = true
                )
            }
            subcommand("list", "Lists all reminders") {
                option<User>(
                    name = "user",
                    description = "The user to list reminders for",
                    required = true
                )
            }
            subcommand("info", "Gets info about a reminder") {
                option<String>(
                    name = "id",
                    description = "The id of the reminder to get info about",
                    required = true
                )
            }
        }
    }

    override suspend fun onSlashCommand(event: SlashCommandInteractionEvent) {
        when (event.commandPath) {
            "${this.name}/add" -> handleAddReminder(event)
            "${this.name}/cancel" -> handleCancelReminder(event)
            "${this.name}/list" -> handleListReminders(event)
            "${this.name}/info" -> handleInfoReminder(event)
        }
    }

    private suspend fun handleAddReminder(event: SlashCommandInteractionEvent) {
        val timeArg = event.getOption("time")!!.asString
        val currentTime = Instant.now().toEpochMilli()

        val duration = try {
            Duration.parse(timeArg).plus(Duration.parse("${currentTime}ms")).inWholeSeconds
        } catch (e: IllegalArgumentException) {
            event.deferReply(true)
                .setContent("Invalid time format")
                .queue()
            return
        }

        val user = event.user
        val message = event.getOption("message")!!.asString
        val hash = event.id.hashString("MD5").slice(0..7)

        event.deferReply()
            .setContent(
                """
                I will remind you in <t:$duration:R> at <t:$duration:F>
                *If you need to cancel or get info for this reminder, use the id: `$hash`*
                """.trimIndent()
            )
            .queue()

        bot.database.getCollection<ReminderDTO>().insertOne(
            ReminderDTO(
                reminder_id = hash,
                member_id = user.id,
                channel_id = event.channel.id,
                message = message,
                created = currentTime / 1000,
                expiry = duration,
            )
        )

        bot.reminderService.requestUpdate()
    }

    private suspend fun handleCancelReminder(event: SlashCommandInteractionEvent) {
        val reminderId = event.getOption("id")!!.asString
        val reminder = bot.database.getCollection<ReminderDTO>().findOne(ReminderDTO::reminder_id eq reminderId)

        val content = when (reminder?.member_id) {
            null -> "No reminder found with id: $reminderId"
            event.user.id -> {
                bot.database.getCollection<ReminderDTO>().deleteOne(ReminderDTO::reminder_id eq reminderId)

                "Successfully cancelled reminder for <@${reminder.member_id}> ${reminder.message.truncate(20)}"
            }

            else -> "You can only cancel your own reminders"
        }

        event.deferReply(true)
            .setContent(content)
            .queue()
    }

    private suspend fun handleListReminders(event: SlashCommandInteractionEvent) {
        val user = event.getOption("user")!!.asUser
        val reminders = bot.database.getCollection<ReminderDTO>().find(ReminderDTO::member_id eq user.id).toList()

        if (reminders.isEmpty()) {
            event.deferReply(true)
                .setContent("No reminders found for user: ${user.name}")
                .queue()
            return
        }

        event.deferReply()
            .addEmbeds(
                Embed {
                    title = "**Reminders for user: ${user.name}**"
                    description = reminders.subList(0, min(10, reminders.size)).mapIndexed { index, remindMe ->
                        """
                        ${index + 1}. ${remindMe.message.truncate(20)}
                        $SPACE_1EM*id: `${remindMe.reminder_id}`*
                        """.trimIndent()
                    }.joinToString("\n")
                    color = Colors.EMERALD_400.hex
                }
            )
            .queue()
    }

    private suspend fun handleInfoReminder(event: SlashCommandInteractionEvent) {
        val reminderId = event.getOption("id")!!.asString
        val reminder = bot.database.getCollection<ReminderDTO>().findOne(ReminderDTO::reminder_id eq reminderId)

        val (ephemeral, content) = when (reminder?.member_id) {
            null -> true to "No reminder found with id: $reminderId"
            event.user.id -> {
                false to """
                    Author **❱** <@${reminder.member_id}>
                    Message **❱** ${reminder.message}
                    id **❱** `${reminder.reminder_id}`
                    Created **❱** <t:${reminder.created}:R>
                    Expiry **❱** <t:${reminder.expiry}:R>
                """.trimIndent()
            }

            else -> true to "You can only get info for your own reminders"
        }

        event.deferReply(ephemeral)
            .setContent(content)
            .setAllowedMentions(listOf(Message.MentionType.CHANNEL))
            .queue()
    }
}
