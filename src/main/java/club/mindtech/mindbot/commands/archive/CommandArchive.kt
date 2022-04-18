package club.mindtech.mindbot.commands.archive

import club.mindtech.mindbot.commands.BaseCommand
import club.mindtech.mindbot.util.notNull
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.ThreadChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData

class CommandArchive : BaseCommand(
    "archive",
    "Makes a nice embed and sends it to archive channel",
    "archive <name> <made-by> <description> <version> <attachment> <file> [attachment] [attachment] [file] [file]"
) {
    override fun getCommandData(): SlashCommandData {
        val data = super.getCommandData()
        data.addSubcommands(
            SubcommandData("create", "The creator of the contraption.")
                .addOptions(
                    OptionData(OptionType.STRING, "name", "The name of the contraption.", true),
                    OptionData(OptionType.MENTIONABLE, "made-by", "The creator of the contraption.", true),
                    OptionData(OptionType.STRING, "description", "The description of the contraption.", true),
                    OptionData(OptionType.STRING, "version", "The version of the contraption.", true),
                    OptionData(OptionType.ATTACHMENT, "image-1", "The attachment of the contraption.", true),
                    OptionData(OptionType.ATTACHMENT, "file-1", "The file of the contraption.", true),
                    OptionData(OptionType.ATTACHMENT, "image-2", "Secondary attachment of the contraption."),
                    OptionData(OptionType.ATTACHMENT, "image-3", "Third attachment of the contraption."),
                    OptionData(OptionType.ATTACHMENT, "file-2", "Secondary file of the contraption."),
                    OptionData(OptionType.ATTACHMENT, "file-3", "Third file of the contraption.")
                )
        )
        return data
    }

    override fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val archiveName = notNull(event.getOption("name")).asString
        val archiveMadeBy = notNull(event.getOption("made-by")).asMentionable.asMention
        val archiveVersion = notNull(event.getOption("version")).asString
        val archiveDescription = notNull(event.getOption("description")).asString
        val attachments = event
            .getOptionsByType(OptionType.ATTACHMENT)
            .groupBy { it.name.substring(0, it.name.indexOf("-")) }
            .mapValues { it.value.map { v -> v.asAttachment } }

        val descriptionEmbed = EmbedBuilder()
            .setTitle(archiveName)
            .addField("Made by:", archiveMadeBy, true)
            .addField("Version:", archiveVersion, true)
            .addField("Description:", archiveDescription.replace("\\n", "\n"), false)
            .setImage(attachments["image"]!![0].url)
            .setFooter("Created at: " + event.timeCreated)
            .setColor(0x38BDF8)
            .build()

        val fileEmbed = EmbedBuilder()
            .setTitle(":page_facing_up: Attachments")
            .setColor(0xFB7185)
            .setDescription(
                attachments["file"]!!.joinToString("\n") { "• [${it.fileName}](${it.url})" }
            ).build()

        val channel = event.channel

        channel.sendMessageEmbeds(descriptionEmbed, fileEmbed)
            .queue { message: Message -> makeImageThread(archiveName, attachments["image"]!!, message) }
        event.deferReply(true)
            .setContent("Archived!")
            .queue()
    }

    private fun makeImageThread(name: String, images: List<Attachment>, message: Message) {
        message.createThreadChannel(name)
            .queue { thread: ThreadChannel -> sendImages(thread, images) }
    }

    private fun sendImages(thread: ThreadChannel, images: List<Attachment>) {
        thread.sendMessageEmbeds(
            images.stream().map { attachment: Attachment -> makeImageEmbed(attachment) }.toList()
        ).queue()
    }

    private fun makeImageEmbed(attachment: Attachment): MessageEmbed {
        return EmbedBuilder()
            .setTitle(attachment.fileName)
            .setColor(0xF0ABFC)
            .setImage(attachment.url)
            .build()
    }
}
