package tempbot;

import java.text.DecimalFormat;
import java.util.List;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.tinylog.Logger;
import tempbot.engine.UserInputProcessor;
import tempbot.engine.ProcessingResult;
import tempbot.engine.ProcessingResult.ConvertedValues;
import tempbot.engine.ProcessingResult.ProcessingError;
import tempbot.engine.ProcessingResult.ProcessingError.DimensionMismatch;
import tempbot.engine.ProcessingResult.ProcessingError.SystemError;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange;
import tempbot.engine.UnitValue;

import static tempbot.Constants.PRECISION;
import static tempbot.Util.panicToStdErr;

/**
 * Interface responsible for translating Discord actions into processor operations
 */
public class BotEventHandler extends ListenerAdapter {

	private final UserInputProcessor processor;
	private final DecimalFormat format;
	private final String helpText;

	public BotEventHandler(@NonNull final UserInputProcessor processor) {
		this.processor = processor;

		format = new DecimalFormat("###,###,###,###." + "#".repeat(PRECISION));
		helpText = buildHelpText(processor);
	}

	@Override
	public void
	onMessageReceived(final MessageReceivedEvent event) {
		if (!event.getAuthor().isBot()) {
			handleMessage(event.getMessage());
		}
	}

	@Override
	public void
	onSlashCommandInteraction(final SlashCommandInteractionEvent event) {
		switch (event.getName()) {
			case "help" -> displayHelp(event);
			case "convert" -> handleConversion(event);
			default -> handleUnknownEvent(event);
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
		formatProcessingResults(results, processingOutput);

		if (processingOutput.length() > 0) {
			message.getChannel().sendMessage(processingOutput.toString()).queue();
		}
	}

	public void
	displayHelp(final SlashCommandInteractionEvent commandEvent) {
		commandEvent.reply(helpText).setEphemeral(true).queue();
	}

	public void
	handleConversion(final SlashCommandInteractionEvent commandEvent) {

	}

	public void
	handleUnknownEvent(final SlashCommandInteractionEvent commandEvent) {
		Logger.error(String.format(
			"A command was invoked that has no defined handler: %s",
			commandEvent.getName()
		));
		commandEvent
			.reply("""
				Sorry! The system does not have a definition for the command you attempted to \
				invoke. This error has been logged for the developers of this bot."""
			).setEphemeral(true)
			.queue();
	}

//	private void
//	createDimensionListingMessage(Dimension dimension, StringBuilder message) {
//		message.append(DIMENSION_HELP_TEXT)
//			.append("\n> ")
//			.append(dimension.getUnits()
//				.map(unit -> {
//					return new StringBuilder()
//						.append("**")
//						.append(unit.getFullName())
//						.append("** ")
//						.append("(")
//						.append(unit.getDetectableNames().stream()
//							.collect(Collectors.joining(", "))
//						).append(")");
//				})
//				.collect(Collectors.joining("\n> "))
//			);
//	}

//	private void
//	createGeneralHelpMessage(StringBuilder message) {
//		message.append(HELP_TEXT)
//			.append("\n")
//			.append(DIMENSION_EXPLAIN_TEXT)
//			.append("\n> ")
//			.append(processor.getDimensions()
//				.map(Dimension::getName)
//				.collect(Collectors.joining("\n> "))
//			);
//	}

	private void
	formatProcessingResults(final List<ProcessingResult> results, final StringBuilder output) {
		for (final ProcessingResult result : results) {
			final var standardOutput = new StringBuilder();
			final var errorOutput = new StringBuilder();

			if (result instanceof final ConvertedValues conversions) {
				formatResults(conversions, standardOutput);
			} else if (result instanceof final ProcessingError error) {
				formatError(error, errorOutput);
			} else {
				panicToStdErr("Attempted to format a result which was not a known result class.");
			}

			output.append(standardOutput);
			output.append(errorOutput);
			// trailing newline is trimmed by Discord
			output.append("\n");
		}
	}

	private void
	formatResults(final ConvertedValues result, final StringBuilder output) {
		if (result.values().size() > 0) {
			addUnitValueString(result.sourceValue(), output);
			output.append(" = ");

			if (result.values().size() == 1) {
				addUnitValueString(result.values().get(0), output);
			}
			else {
				output.append("\n");
				for (final UnitValue value : result.values()) {
					output.append("> ");
					addUnitValueString(value, output);
					output.append("\n");
				}
			}
		}
	}

	private void
	formatError(final ProcessingError error, final StringBuilder output) {
		if (error instanceof final UnitOutOfRange rangeError) {
			addUnitValueString(rangeError.sourceValue(), output);
			output.append(" is ");
			output.append(switch (rangeError.limitType()) {
				case MAXIMUM -> "greater than ";
				case MINIMUM -> "less than ";
			});
			addUnitValueString(rangeError.rangeLimitingValue(), output);
			output.append(", the ");
			output.append(switch (rangeError.limitType()) {
				case MAXIMUM -> "maximum";
				case MINIMUM -> "minimum";
			});
			output.append(".");
		} else if (error instanceof final DimensionMismatch dimensionError) {
			output.append("Can't convert units from ")
				.append(dimensionError.sourceDimension().getName())
				.append(" to ")
				.append(dimensionError.destinationDimension().getName())
				.append(".");
		} else if (error instanceof SystemError) {
			// errors should be logged at the point of creation
			output.append("A system error occurred while processing a unit conversion.");
		}
	}

	private static String
	buildHelpText(UserInputProcessor processor) {
		final var helpText = new StringBuilder("""
			TempBot is a unit-conversion bot. It can convert a variety of units to make your \
			conversations easier with friends who might use different temperature units from you, \
			or just satisfy your curiosity.

			TempBot will automatically convert units of the following types if it sees them in a channel:
			""");

		processor.getEagerDimensions().stream().forEach(name -> {
			helpText.append("- ")
				.append(name)
				.append("\n");
		});

		helpText.append("\n")
			.append("TempBot can also convert units of the following types when asked:\n");

		processor.getNonEagerDimensions().stream().forEach(name -> {
			helpText.append("- ")
				.append(name)
				.append("\n");
		});

		helpText.append("\n")
			.append("""
				To see what units TempBot knows about for each type, invoke `/help` and include \
				the name of one of these unit types.""");

		return helpText.toString();
	}

	private void
	addUnitValueString(final UnitValue value, final StringBuilder buffer) {
		buffer.append("**")
			.append(format.format(value.value()))
			.append(" ")
			.append(value.unit().getShortName())
			.append("**");
	}

}
