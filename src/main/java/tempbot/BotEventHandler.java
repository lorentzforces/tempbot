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
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.tinylog.Logger;
import tempbot.buttons.BotButton;
import tempbot.buttons.ShowMessageButton;
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
	private final Map<String, BotButton> buttonMap;

	public BotEventHandler(@NonNull final UserInputProcessor processor) {
		this.processor = processor;

		final var showMessageButton = new ShowMessageButton();
		this.buttonMap =
			Stream.of(showMessageButton)
			.collect(Collectors.toMap(
				BotButton::getName,
				Function.identity()
			));
		this.commandMap =
			Stream.of(
				new HelpCommand(processor),
				new ConvertCommand(processor, showMessageButton.getRegistrationObject())
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
	onMessageReceived(@NonNull MessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) {
			handleMessage(event.getMessage());
		}
	}

	// TODO: support autocompletion for obvious things like destination unit, dimension listing

	@Override
	public void
	onSlashCommandInteraction(@NonNull SlashCommandInteractionEvent event) {
		switch (commandMap.get(event.getName())) {
			case null -> handleUnknownSlashEvent(event);
			case SlashCommand c -> c.handleCommandEvent(event);
		}
	}

	@Override
	public void
	onButtonInteraction(@NonNull ButtonInteractionEvent event) {
		switch (buttonMap.get(event.getComponentId())) {
			case null -> handleUnknownButtonEvent(event);
			case BotButton b -> b.handleButtonEvent(event);
		}
	}

	public void
	handleMessage(@NonNull Message message) {
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

	private static final String GENERIC_ERROR_MESSAGE =
		"⚠️ Sorry! Something went wrong. This error has been logged for the developers of this bot.";

	private void
	handleUnknownSlashEvent(@NonNull SlashCommandInteractionEvent commandEvent) {
		Logger.error(String.format(
			"A command was invoked that has no defined handler: %s",
			commandEvent.getName()
		));
		commandEvent.reply(GENERIC_ERROR_MESSAGE).setEphemeral(true).queue();
	}

	private void
	handleUnknownButtonEvent(@NonNull ButtonInteractionEvent buttonEvent) {
		Logger.error(String.format(
			"A button was invoked that has no defined handler: %s",
			buttonEvent.getComponentId()
		));
		// clear any buttons off the triggering message
		buttonEvent.deferEdit().setComponents().queue();
		buttonEvent.reply(GENERIC_ERROR_MESSAGE).setEphemeral(true).queue();
	}

}
