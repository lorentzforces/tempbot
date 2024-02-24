package tempbot;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.tinylog.Logger;
import tempbot.commands.ConvertCommand;
import tempbot.commands.HelpCommand;
import tempbot.commands.SlashCommand;
import tempbot.engine.ProcessingResult;
import tempbot.engine.UserInputProcessor;

/**
 * Interface responsible for translating Discord actions into processor operations
 */
public class BotEventHandler extends ListenerAdapter {

	private final UserInputProcessor processor;
	private final Map<String, SlashCommand> commandMap;

	public BotEventHandler(@NonNull final UserInputProcessor processor) {
		this.processor = processor;
		this.commandMap =
			Stream.of(
				new HelpCommand(processor),
				new ConvertCommand(processor)
			).collect(Collectors.toMap(
				SlashCommand::getName,
				Function.identity()
			));
	}

	public Collection<SlashCommand>
	getCommands() {
		// map this to a new collection since values() provides a collection backed by the map
		// itself
		return commandMap.values().stream().collect(Collectors.toList());
	}

	@Override
	public void
	onMessageReceived(final MessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) {
			handleMessage(event.getMessage());
		}
	}

	// TODO: support autocompletion for obvious things like destination unit, dimension listing

	@Override
	public void
	onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
		switch (commandMap.get(event.getName())) {
			case null -> handleUnknownEvent(event);
			case SlashCommand c -> c.handleCommandEvent(event);
		}
	}

	public void
	handleMessage(final Message message) {
		final String messageContent = message.getContentStripped();
		Logger.debug("Message handling: \"" + messageContent + "\"");
		if (messageContent.length() == 0) {
			return;
		}

		final List<ProcessingResult> results = processor.processMessage(messageContent);
		final StringBuilder processingOutput = new StringBuilder();
		DiscordFormatting.formatProcessingResults(results, processingOutput);

		if (processingOutput.length() > 0) {
			message.getChannel().sendMessage(processingOutput.toString()).queue();
		}
	}

	public void
	handleUnknownEvent(final SlashCommandInteractionEvent commandEvent) {
		Logger.error(String.format(
			"A command was invoked that has no defined handler: %s",
			commandEvent.getName()
		));
		commandEvent
			.reply("""
				Sorry! Something went wrong. This error has been logged for the developers of this \
				bot. \
			""")
			.setEphemeral(true)
			.queue();
	}

}
