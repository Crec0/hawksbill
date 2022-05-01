package club.mindtech.mindbot.commands.archive

import club.mindtech.mindbot.commands.BaseCommand
import club.mindtech.mindbot.util.notNull
import dev.minn.jda.ktx.Embed
import dev.minn.jda.ktx.interactions.option
import dev.minn.jda.ktx.interactions.subcommand
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

class CommandArchive : BaseCommand(
    "archive",
    "Makes a nice embed and sends it to archive channel",
    "archive <name> <made-by> <description> <version> <attachment> <file> [attachment] [attachment] [file] [file]"
) {
    override fun getCommandData(): SlashCommandData {
        val data = super.getCommandData().subcommand("create", "Create a new archive") {
            option<String>("name", "The name of the contraption.", true)
            option<IMentionable>("made-by", "Who made the contraption.", true)
            option<String>("description", "The description of the contraption.", true)
            option<String>("version", "The version of the contraption.", true)
            option<Attachment>("image-1", "The attachment of the contraption.", true)
            option<Attachment>("file-1", "The file of the contraption.", true)
            option<Attachment>("image-2", "The attachment of the contraption.")
            option<Attachment>("file-2", "The file of the contraption.")
            option<Attachment>("image-3", "The attachment of the contraption.")
            option<Attachment>("file-3", "The file of the contraption.")
        }
        return data
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val archiveName = notNull(event.getOption("name")).asString
        val archiveMadeBy = notNull(event.getOption("made-by")).asMentionable.asMention
        val archiveVersion = notNull(event.getOption("version")).asString
        val archiveDescription = notNull(event.getOption("description")).asString
        val attachments =
            event.getOptionsByType(OptionType.ATTACHMENT)
                .groupBy { it.name.substring(0, it.name.indexOf("-")) }
                .mapValues { it.value.map { v -> v.asAttachment } }

        val descriptionEmbed = Embed {
            title = archiveName
            color = 0x38BDF8
            field {
                name = "Made by"
                value = archiveMadeBy
            }
            field {
                name = "Version"
                value = archiveVersion
            }
            field {
                name = "Description"
                value = archiveDescription
            }
            image = attachments["image-1"]!![0].url
            footer {
                name = "Created at: ${event.timeCreated}"
                iconUrl = event.user.avatarUrl
            }
        }

        val fileEmbed = Embed {
            title = ":page_facing_up: Attachments"
            color = 0xFB7185
            description = attachments["file"]!!.joinToString("\n") { "• [${it.fileName}](${it.url})" }
        }

        val channel = event.channel

        channel.sendMessageEmbeds(descriptionEmbed, fileEmbed).queue {
            makeImageThread(archiveName, attachments["image"]!!, it)
        }
        event.deferReply(true).setContent("Archived!").queue()
    }

    private fun makeImageThread(name: String, images: List<Attachment>, message: Message) {
        message.createThreadChannel(name).queue { sendImages(it, images) }
    }

    private fun sendImages(thread: ThreadChannel, images: List<Attachment>) {
        thread.sendMessageEmbeds(
            images.map { makeImageEmbed(it) }.toList()
        ).queue()
    }

    private fun makeImageEmbed(attachment: Attachment): MessageEmbed {
        return Embed {
            title = attachment.fileName
            color = 0xF0ABFC
            image = attachment.url
        }
    }
}
