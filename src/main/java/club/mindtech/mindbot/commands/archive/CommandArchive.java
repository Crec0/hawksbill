package club.mindtech.mindbot.commands.archive;

import club.mindtech.mindbot.commands.BaseCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static club.mindtech.mindbot.util.BotHelper.notNull;

public class CommandArchive extends BaseCommand {
	public CommandArchive() {
		super(
			"archive",
			"Makes a nice embed and sends it to archive channel",
			"archive <name> <made-by> <description> <version> <attachment> <file> [attachment] [attachment] [file] [file]"
		);
	}

	@Override
	public SlashCommandData getCommandData() {
		SlashCommandData data = super.getCommandData();
		data.addSubcommands(
			new SubcommandData("create", "The creator of the contraption.")
				.addOptions(
					new OptionData(OptionType.STRING, "name", "The name of the contraption.", true),
					new OptionData(OptionType.MENTIONABLE, "made-by", "The creator of the contraption.", true),
					new OptionData(OptionType.STRING, "description", "The description of the contraption.", true),
					new OptionData(OptionType.STRING, "version", "The version of the contraption.", true),
					new OptionData(OptionType.ATTACHMENT, "image-1", "The attachment of the contraption.", true),
					new OptionData(OptionType.ATTACHMENT, "file-1", "The file of the contraption.", true),
					new OptionData(OptionType.ATTACHMENT, "image-2", "Secondary attachment of the contraption."),
					new OptionData(OptionType.ATTACHMENT, "image-3", "Third attachment of the contraption."),
					new OptionData(OptionType.ATTACHMENT, "file-2", "Secondary file of the contraption."),
					new OptionData(OptionType.ATTACHMENT, "file-3", "Third file of the contraption.")
				)
		);
		return data;
	}

	@Override
	public void onSlashCommand(SlashCommandInteractionEvent event) {
		String archiveName = notNull(event.getOption("name")).getAsString();
		String archiveMadeBy = notNull(event.getOption("made-by")).getAsMentionable().getAsMention();
		String archiveVersion = notNull(event.getOption("version")).getAsString();
		String archiveDescription = notNull(event.getOption("description")).getAsString();

		Map<String, List<Message.Attachment>> attachments = event
			.getOptionsByType(OptionType.ATTACHMENT)
			.stream()
			.collect(
				Collectors.groupingBy(
					f -> f.getName().substring(0, f.getName().indexOf("-")),
					Collectors.mapping(
						optionMapping -> notNull(optionMapping).getAsAttachment(),
						Collectors.toList()
					)
				)
			);


		MessageEmbed descriptionEmbed = new EmbedBuilder()
			.setTitle(archiveName)
			.addField("Made by:", archiveMadeBy, true)
			.addField("Version:", archiveVersion, true)
			.addField("Description:", archiveDescription.replace("\\n", "\n"), false)
			.setImage(attachments.get("image").get(0).getUrl())
			.setFooter("Created at: " + event.getTimeCreated())
			.setColor(0x38BDF8)
			.build();

		MessageEmbed fileEmbed = new EmbedBuilder()
			.setTitle(":page_facing_up: Attachments")
			.setColor(0xFB7185)
			.setDescription(
				attachments.get("file")
 					 .stream()
					 .map(attachment -> "[" + attachment.getFileName() + "](" + attachment.getUrl() + ")")
					 .collect(Collectors.joining("\n"))
			).build();

		MessageChannel channel = event.getChannel();
		channel.sendMessageEmbeds(descriptionEmbed, fileEmbed)
			   .queue(message -> makeImageThread(archiveName, attachments.get("image"), message));

		event.deferReply(true)
			 .setContent("Archived!")
			 .queue();
	}

	private void makeImageThread(String name, List<Message.Attachment> images, Message message) {
		message.createThreadChannel(name)
			   .queue(thread -> sendImages(thread, images));
	}

	private void sendImages(ThreadChannel thread, List<Message.Attachment> images) {
		thread.sendMessageEmbeds(
			images.stream().map(this::makeImageEmbed).toList()
		).queue();
	}

	private MessageEmbed makeImageEmbed(Message.Attachment attachment) {
		return new EmbedBuilder()
			.setTitle(attachment.getFileName())
			.setColor(0xF0ABFC)
			.setImage(attachment.getUrl())
			.build();
	}
}
