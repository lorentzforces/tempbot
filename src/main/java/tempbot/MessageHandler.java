package tempbot;

import java.text.DecimalFormat;
import java.util.List;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Message;
import org.tinylog.Logger;
import tempbot.engine.FullTextMessageProcessor;
import tempbot.engine.ProcessingResult;
import tempbot.engine.ProcessingResult.ConvertedValues;
import tempbot.engine.ProcessingResult.ProcessingError;
import tempbot.engine.ProcessingResult.ProcessingError.DimensionMismatch;
import tempbot.engine.ProcessingResult.ProcessingError.SystemError;
import tempbot.engine.ProcessingResult.ProcessingError.UnitOutOfRange;
import tempbot.engine.UnitValue;

import static tempbot.Constants.PRECISION;
import static tempbot.Util.panicToStdErr;

public class MessageHandler {

	private final FullTextMessageProcessor processor;
	private final DecimalFormat format;

	public MessageHandler(@NonNull FullTextMessageProcessor processor) {
		this.processor = processor;

		format = new DecimalFormat("###,###,###,###." + "#".repeat(PRECISION));
	}

	public void
	handle(Message message) {
		String messageContent = message.getContentStripped();
		Logger.debug("Message handling: \"" + messageContent + "\"");
		if (messageContent.length() == 0) {
			return;
		}

		List<ProcessingResult> results = processor.processMessage(messageContent);
		StringBuilder processingOutput = new StringBuilder();
		formatProcessingResults(results, processingOutput);

		if (processingOutput.length() > 0) {
			message.getChannel().sendMessage(processingOutput.toString()).queue();
		}
	}

//	private void
//	handlePotentialHelpResponse(String messageContents, User them) {
//		Matcher botMentionMatcher = rawBotMentionPattern.matcher(messageContents);
//		String contentsWithoutBotMention = botMentionMatcher.replaceAll("").trim();
//		Matcher helpMatcher = helpPattern.matcher(messageContents);
//
//		StringBuilder helpMessage = new StringBuilder();
//		if (helpMatcher.find()) {
//			String dimensionString = helpMatcher.group(PATTERN_DIMENSION_GROUP);
//			if (dimensionString == null) {
//				createGeneralHelpMessage(helpMessage);
//			}
//			else {
//				createDimensionListingMessage(
//					processor.getDimensionFromName(dimensionString),
//					helpMessage
//				);
//			}
//		}
//		else if (contentsWithoutBotMention.length() == 0) {
//			createGeneralHelpMessage(helpMessage);
//		}
//
//		if (helpMessage.length() > 0) {
//			them.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(helpMessage.toString()));
//		}
//	}

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
	formatProcessingResults(List<ProcessingResult> results, StringBuilder output) {
		for (ProcessingResult result : results) {
			final var standardOutput = new StringBuilder();
			final var errorOutput = new StringBuilder();

			if (result instanceof ConvertedValues conversions) {
				formatResults(conversions, standardOutput);
			} else if (result instanceof ProcessingError error) {
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
	formatResults(ConvertedValues result, StringBuilder output) {
		if (result.values().size() > 0) {
			addUnitValueString(result.sourceValue(), output);
			output.append(" = ");

			if (result.values().size() == 1) {
				addUnitValueString(result.values().get(0), output);
			}
			else {
				output.append("\n");
				for (UnitValue value : result.values()) {
					output.append("> ");
					addUnitValueString(value, output);
					output.append("\n");
				}
			}
		}
	}

	private void
	formatError(ProcessingError error, StringBuilder output) {
		if (error instanceof UnitOutOfRange rangeError) {
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
		} else if (error instanceof DimensionMismatch dimensionError) {
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

	private void
	addUnitValueString(UnitValue value, StringBuilder buffer) {
		buffer.append("**")
			.append(format.format(value.value()))
			.append(" ")
			.append(value.unit().getShortName())
			.append("**");
	}

	private static final String DIMENSION_EXPLAIN_TEXT =
		"Right now, I can do conversions for these categories (tag or DM me and say "
		+ "\"help *<category name>*\" to see the units I can convert in each category:";

	private static final String DIMENSION_HELP_TEXT =
		"These are the units of that type I can convert:";


}
